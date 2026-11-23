package app.dlxplayer.shared.jellyfin

import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder

/**
 * An authenticated Jellyfin session, started from a successful login via
 * [JellyfinClient.session].
 *
 * Every request the session makes is authorized with the login's access
 * token (`Authorization: MediaBrowser ..., Token="..."`), so it only works
 * while the server considers that token valid.
 */
class JellyfinSession internal constructor(
    private val client: JellyfinClient,
    /** Token issued by the login that started this session. */
    val accessToken: String,
    /** Server-side id of the logged-in user, from the login response. */
    val userId: String? = null,
) {
    /**
     * Runs an authenticated call, translating a 401 into
     * [JellyfinSessionExpiredException]: a token the server just rejected,
     * as opposed to wrong credentials at login time.
     */
    private suspend fun <T> authorized(request: suspend () -> T): T =
        try {
            request()
        } catch (error: JellyfinHttpException) {
            if (error.statusCode == HttpStatusCode.Unauthorized) {
                throw JellyfinSessionExpiredException(error)
            }
            throw error
        }

    /** The user this session is logged in as, per the server. */
    suspend fun me(): UserDto = authorized { client.apiGet(path = "Users/Me", token = accessToken) }

    /** Server details as reported to authenticated clients. */
    suspend fun systemInfo(): SystemInfo = authorized { client.apiGet(path = "System/Info", token = accessToken) }

    /** The user's libraries (views), e.g. Movies, Shows, Music. */
    suspend fun views(): List<LibraryDto> =
        authorized {
            client
                .apiGet<PagedItems<LibraryDto>>(
                    path = "Users/${requireNotNull(userId) { "login reported no user id" }}/Views",
                    token = accessToken,
                ).items
        }

    /** Pages through the user's items, e.g. one library's contents. */
    suspend fun items(query: ItemsQuery = ItemsQuery()): PagedItems<ItemDto> =
        authorized {
            client.apiGet<PagedItems<ItemDto>>(
                path = "Users/${requireNotNull(userId) { "login reported no user id" }}/Items",
                token = accessToken,
                parameters = query.toParameters(),
            )
        }

    /** Video items with a saved playback position, newest activity first. */
    suspend fun resumeItems(): PagedItems<ItemDto> =
        authorized {
            client.apiGet<PagedItems<ItemDto>>(
                path = "Users/${requireNotNull(userId) { "login reported no user id" }}/Items/Resume",
                token = accessToken,
                parameters = listOf("mediaTypes" to "Video"),
            )
        }

    /** The episode of any show the user should watch next. */
    suspend fun nextUp(): PagedItems<ItemDto> =
        authorized {
            client.apiGet<PagedItems<ItemDto>>(
                path = "Shows/NextUp",
                token = accessToken,
                parameters = listOf("UserId" to requireNotNull(userId) { "login reported no user id" }),
            )
        }

    /** The seasons of a show, in order. */
    suspend fun seasons(seriesId: String): PagedItems<ItemDto> =
        authorized {
            client.apiGet<PagedItems<ItemDto>>(
                path = "Shows/$seriesId/Seasons",
                token = accessToken,
                parameters = listOf("UserId" to requireNotNull(userId) { "login reported no user id" }),
            )
        }

    /** All episodes of a show, in season and episode order. */
    suspend fun episodes(seriesId: String): PagedItems<ItemDto> =
        authorized {
            client.apiGet<PagedItems<ItemDto>>(
                path = "Shows/$seriesId/Episodes",
                token = accessToken,
                parameters = listOf("UserId" to requireNotNull(userId) { "login reported no user id" }),
            )
        }

    /**
     * Asks the server how to play an item: its media sources with the
     * direct-play/direct-stream/transcode decision, plus a play session
     * id for the subsequent playback reports.
     */
    suspend fun playbackInfo(
        itemId: String,
        deviceProfile: DeviceProfile =
            DeviceProfile(
                maxStreamingBitrate = 120_000_000,
                directPlayProfiles = listOf(DirectPlayProfile(container = "mp4", type = "Video")),
            ),
    ): PlaybackInfo =
        authorized {
            client.apiPostJson<PlaybackInfo>(
                path = "Items/$itemId/PlaybackInfo",
                token = accessToken,
                body = PlaybackInfoRequest(deviceProfile = deviceProfile),
            )
        }

    /**
     * URL of one of an item's images, ready for players and image loaders
     * that cannot send headers: the session token rides in the query as
     * `api_key` (confirmed against the reference server), sizing via
     * `maxWidth`/`maxHeight`.
     */
    fun imageUrl(
        itemId: String,
        imageType: String,
        maxWidth: Int? = null,
        maxHeight: Int? = null,
    ): String =
        URLBuilder("${client.apiBase}/Items/$itemId/Images/$imageType")
            .apply {
                parameters.append("api_key", accessToken)
                maxWidth?.let { parameters.append("maxWidth", it.toString()) }
                maxHeight?.let { parameters.append("maxHeight", it.toString()) }
            }.buildString()

    /**
     * Progressive video URL — the original file, playable when the client
     * can direct play the container. Stream routes accept the token only
     * as `ApiKey` (verified live; `api_key` is rejected by the HLS and
     * universal routes).
     */
    fun videoStreamUrl(
        itemId: String,
        container: String = "mp4",
        mediaSourceId: String? = null,
    ): String =
        URLBuilder("${client.apiBase}/Videos/$itemId/stream.$container")
            .apply {
                parameters.append("ApiKey", accessToken)
                mediaSourceId?.let { parameters.append("MediaSourceId", it) }
            }.buildString()

    /** HLS master playlist URL for adaptive playback of a video. */
    fun hlsMasterPlaylistUrl(
        itemId: String,
        mediaSourceId: String? = null,
    ): String = hlsPlaylistUrl(itemId, "master", mediaSourceId)

    /**
     * URL of one HLS segment. Players normally follow the ready-made
     * segment URLs inside the media playlist; this builder covers
     * composing them (route requires the ticks params, verified live).
     */
    fun hlsSegmentUrl(
        itemId: String,
        segmentId: Int,
        actualSegmentLengthTicks: Long,
        mediaSourceId: String? = null,
        runtimeTicks: Long = 0,
        container: String = "ts",
    ): String =
        URLBuilder("${client.apiBase}/Videos/$itemId/hls1/main/$segmentId.$container")
            .apply {
                parameters.append("MediaSourceId", mediaSourceId ?: itemId)
                parameters.append("ApiKey", accessToken)
                parameters.append("runtimeTicks", runtimeTicks.toString())
                parameters.append("actualSegmentLengthTicks", actualSegmentLengthTicks.toString())
            }.buildString()

    /**
     * Universal audio URL — direct streams when [container] matches the
     * source, transcodes otherwise. Carries the device identity like
     * the official web client does.
     */
    fun audioUniversalUrl(
        itemId: String,
        container: String = "mp3",
    ): String =
        URLBuilder("${client.apiBase}/Audio/$itemId/universal")
            .apply {
                parameters.append("DeviceId", client.device.deviceId)
                userId?.let { parameters.append("UserId", it) }
                parameters.append("container", container)
                parameters.append("ApiKey", accessToken)
            }.buildString()

    private fun hlsPlaylistUrl(
        itemId: String,
        playlist: String,
        mediaSourceId: String?,
    ): String =
        URLBuilder("${client.apiBase}/Videos/$itemId/$playlist.m3u8")
            .apply {
                parameters.append("MediaSourceId", mediaSourceId ?: itemId)
                parameters.append("ApiKey", accessToken)
            }.buildString()

    /**
     * Invalidates this session's token on the server. Once it returns,
     * further authenticated calls fail with [JellyfinSessionExpiredException].
     */
    suspend fun logout() {
        authorized { client.apiPost(path = "Sessions/Logout", token = accessToken) }
    }
}
