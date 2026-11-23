package app.dlxplayer.shared.jellyfin

import io.ktor.http.HttpStatusCode

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

    /**
     * Invalidates this session's token on the server. Once it returns,
     * further authenticated calls fail with [JellyfinSessionExpiredException].
     */
    suspend fun logout() {
        authorized { client.apiPost(path = "Sessions/Logout", token = accessToken) }
    }
}
