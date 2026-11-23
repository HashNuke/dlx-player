package app.dlxplayer.shared.jellyfin

import io.ktor.http.HttpStatusCode

/**
 * Thrown when the Jellyfin server answers an API request with an HTTP
 * error status (4xx/5xx). Apps can branch on [statusCode], e.g. an
 * unauthorized response during login means the credentials were wrong.
 */
open class JellyfinHttpException(
    val statusCode: HttpStatusCode,
    cause: Throwable? = null,
) : Exception("Jellyfin request failed with HTTP ${statusCode.value} ${statusCode.description}", cause)

/**
 * Thrown when an authenticated call answers 401: the server no longer
 * accepts this session's access token (logged out elsewhere, server
 * restart, expiry). A failed login stays a plain [JellyfinHttpException],
 * so apps can tell an expired session from wrong credentials.
 */
class JellyfinSessionExpiredException(
    cause: Throwable? = null,
) : JellyfinHttpException(HttpStatusCode.Unauthorized, cause)
