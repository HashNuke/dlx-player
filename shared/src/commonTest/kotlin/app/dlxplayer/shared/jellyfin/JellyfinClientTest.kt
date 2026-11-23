package app.dlxplayer.shared.jellyfin

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JellyfinClientTest {
    @Test
    fun `public system info is fetched from the server`() =
        runTest {
            var requestedPath: String? = null
            var requestedMethod: HttpMethod? = null
            val engine =
                MockEngine { request ->
                    requestedPath = request.url.encodedPath
                    requestedMethod = request.method
                    respond(
                        content = PUBLIC_SYSTEM_INFO_JSON,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType to listOf(ContentType.Application.Json.toString())),
                    )
                }

            val client =
                JellyfinClient(
                    baseUrl = "http://192.168.1.10:8096",
                    device = TEST_DEVICE,
                    engine = engine,
                )

            val info = client.publicSystemInfo()

            assertEquals("/System/Info/Public", requestedPath)
            assertEquals(HttpMethod.Get, requestedMethod)
            assertEquals("DLX Reference", info.serverName)
            assertEquals("12.4.0", info.version)
            assertEquals("Jellyfin Server", info.productName)
            assertEquals("4a828cdb4fc0494184c5d45b616d6423", info.id)
            assertEquals(true, info.startupWizardCompleted)
        }

    @Test
    fun `authenticate by name posts credentials and parses the token`() =
        runTest {
            var requestedMethod: HttpMethod? = null
            var requestedPath: String? = null
            var authorizationHeader: String? = null
            var requestBody: String? = null
            val engine =
                MockEngine { request ->
                    requestedMethod = request.method
                    requestedPath = request.url.encodedPath
                    authorizationHeader = request.headers[HttpHeaders.Authorization]
                    requestBody = request.body.toByteArray().decodeToString()
                    respond(
                        content = AUTHENTICATION_RESULT_JSON,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType to listOf(ContentType.Application.Json.toString())),
                    )
                }

            val client =
                JellyfinClient(
                    baseUrl = "http://192.168.1.10:8096/",
                    device = TEST_DEVICE,
                    engine = engine,
                )

            val result = client.authenticateByName(username = "root", password = "topsecret")

            assertEquals(HttpMethod.Post, requestedMethod)
            assertEquals("/Users/AuthenticateByName", requestedPath)
            assertEquals(
                """MediaBrowser Client="DLX Player", Device="Pixel 9", DeviceId="device-1", Version="0.1.0"""",
                authorizationHeader,
            )
            assertEquals("""{"Username":"root","Pw":"topsecret"}""", requestBody)
            assertEquals("f6547397831d44718e44f89bc0d66ced", result.accessToken)
            assertEquals("4a828cdb4fc0494184c5d45b616d6423", result.serverId)
            assertEquals("2861f5b8509e46e9a6dca9a1c396501f", result.user?.id)
            assertEquals("root", result.user?.name)
            assertEquals(true, result.user?.hasPassword)
        }

    @Test
    fun `invalid credentials surface as a typed http exception`() =
        runTest {
            val engine =
                MockEngine {
                    respond(
                        content = "Error processing request.",
                        status = HttpStatusCode.Unauthorized,
                        headers = headersOf(HttpHeaders.ContentType to listOf(ContentType.Text.Plain.toString())),
                    )
                }

            val client =
                JellyfinClient(
                    baseUrl = "http://192.168.1.10:8096",
                    device = TEST_DEVICE,
                    engine = engine,
                )

            val error =
                assertFailsWith<JellyfinHttpException> {
                    client.authenticateByName(username = "root", password = "wrong")
                }
            assertEquals(HttpStatusCode.Unauthorized, error.statusCode)
        }

    companion object {
        val TEST_DEVICE =
            DeviceInfo(
                clientName = "DLX Player",
                deviceName = "Pixel 9",
                deviceId = "device-1",
                version = "0.1.0",
            )
    }
}
