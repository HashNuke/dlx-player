package app.dlxplayer.shared.jellyfin

import io.ktor.http.HttpStatusCode

/**
 * Thrown when the Jellyfin server answers an API request with an HTTP
 * error status (4xx/5xx). Apps can branch on [statusCode], e.g. an
 * unauthorized response during login means the credentials were wrong.
 */
class JellyfinHttpException(
    val statusCode: HttpStatusCode,
    cause: Throwable? = null,
) : Exception("Jellyfin request failed with HTTP ${statusCode.value} ${statusCode.description}", cause)
