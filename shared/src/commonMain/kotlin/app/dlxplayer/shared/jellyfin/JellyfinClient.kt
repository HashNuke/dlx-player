package app.dlxplayer.shared.jellyfin

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Identity of the app installation, sent with every authenticated request
 * as the `Authorization: MediaBrowser ...` header.
 */
data class DeviceInfo(
    val clientName: String,
    val deviceName: String,
    val deviceId: String,
    val version: String,
) {
    /** Builds the `MediaBrowser Client="...", Device="...", ...` header value. */
    fun authorizationHeaderValue(token: String? = null): String =
        buildString {
            append("MediaBrowser")
            append(" Client=\"").append(clientName).append('"')
            append(", Device=\"").append(deviceName).append('"')
            append(", DeviceId=\"").append(deviceId).append('"')
            append(", Version=\"").append(version).append('"')
            if (token != null) append(", Token=\"").append(token).append('"')
        }
}

/**
 * Client for a single Jellyfin server.
 *
 * @param baseUrl server root, e.g. `http://192.168.1.10:8096` (with or
 *   without a trailing slash)
 * @param device identifies this app installation to the server
 * @param engine HTTP engine to run on; `null` picks the platform default.
 *   Tests inject Ktor's [io.ktor.client.engine.mock.MockEngine].
 */
class JellyfinClient(
    val baseUrl: String,
    val device: DeviceInfo,
    engine: HttpClientEngine? = null,
) {
    /** Server root without a trailing slash; API routes and image URLs hang off it. */
    internal val apiBase: String = baseUrl.trimEnd('/')

    private val configureClient: HttpClientConfig<*>.() -> Unit = {
        expectSuccess = true
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    internal val httpClient: HttpClient =
        if (engine == null) HttpClient(configureClient) else HttpClient(engine, configureClient)

    /** Runs an API call, translating HTTP error statuses to [JellyfinHttpException]. */
    internal suspend fun <T> apiRequest(request: suspend () -> T): T =
        try {
            request()
        } catch (cause: ResponseException) {
            throw JellyfinHttpException(statusCode = cause.response.status, cause = cause)
        }

    /**
     * GETs an API route, authorized with the device identity and, when the
     * caller holds one, the session's access token.
     */
    internal suspend inline fun <reified T> apiGet(
        path: String,
        token: String? = null,
        parameters: List<Pair<String, String>> = emptyList(),
    ): T =
        apiRequest {
            httpClient
                .get("$apiBase/$path") {
                    header(HttpHeaders.Authorization, device.authorizationHeaderValue(token))
                    parameters.forEach { (key, value) -> parameter(key, value) }
                }.body()
        }

    /**
     * POSTs an API route with a JSON request body, authorized with the
     * device identity and, when the caller holds one, the session token.
     * The response is returned unparsed (report routes answer 204).
     */
    internal suspend fun apiPostJson(
        path: String,
        token: String? = null,
        body: Any? = null,
    ): HttpResponse =
        apiRequest {
            httpClient
                .post("$apiBase/$path") {
                    header(HttpHeaders.Authorization, device.authorizationHeaderValue(token))
                    contentType(ContentType.Application.Json)
                    setBody(body)
                }
        }

    /**
     * POSTs an API route without a request body (e.g. /Sessions/Logout),
     * authorized like [apiGet]. The response body is not read.
     */
    internal suspend fun apiPost(
        path: String,
        token: String? = null,
    ) {
        apiRequest {
            httpClient
                .post("$apiBase/$path") {
                    header(HttpHeaders.Authorization, device.authorizationHeaderValue(token))
                }
        }
    }

    /** Fetches public server details without requiring credentials. */
    suspend fun publicSystemInfo(): PublicSystemInfo =
        apiRequest { httpClient.get("$apiBase/System/Info/Public").body() }

    /** Logs in with a username and password, returning the issued access token. */
    suspend fun authenticateByName(
        username: String,
        password: String,
    ): AuthenticationResult =
        apiRequest {
            httpClient
                .post("$apiBase/Users/AuthenticateByName") {
                    header(HttpHeaders.Authorization, device.authorizationHeaderValue())
                    contentType(ContentType.Application.Json)
                    setBody(AuthenticateByNameRequest(username = username, password = password))
                }.body()
        }

    /**
     * Starts an authenticated session from a successful login. The session
     * authorizes every request with the login's access token.
     */
    fun session(auth: AuthenticationResult): JellyfinSession =
        JellyfinSession(
            client = this,
            accessToken =
                requireNotNull(auth.accessToken) {
                    "authentication result carried no access token; cannot start a session"
                },
            userId = auth.user?.id,
        )
}
