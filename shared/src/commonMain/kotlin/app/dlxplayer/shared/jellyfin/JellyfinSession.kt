package app.dlxplayer.shared.jellyfin

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
) {
    /** The user this session is logged in as, per the server. */
    suspend fun me(): UserDto = client.apiGet(path = "Users/Me", token = accessToken)
}
