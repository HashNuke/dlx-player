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
     * Invalidates this session's token on the server. Once it returns,
     * further authenticated calls fail with [JellyfinSessionExpiredException].
     */
    suspend fun logout() {
        authorized { client.apiPost(path = "Sessions/Logout", token = accessToken) }
    }
}
