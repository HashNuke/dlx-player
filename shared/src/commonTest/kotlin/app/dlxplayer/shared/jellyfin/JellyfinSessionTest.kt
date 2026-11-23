package app.dlxplayer.shared.jellyfin

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Hermetic tests for [JellyfinSession]. Every test logs in through the
 * mock server (replaying the captured authentication response) and then
 * exercises the session's authenticated calls.
 */
class JellyfinSessionTest {
    private val jsonResponseHeaders =
        headersOf(HttpHeaders.ContentType to listOf(ContentType.Application.Json.toString()))

    /** Logs in on the mock server and returns the session it starts. */
    private suspend fun loggedInSession(engine: MockEngine): JellyfinSession {
        val client =
            JellyfinClient(
                baseUrl = "http://192.168.1.10:8096",
                device = TEST_DEVICE,
                engine = engine,
            )
        return client.session(client.authenticateByName(username = "root", password = "topsecret"))
    }

    @Test
    fun `me parses the captured user shape`() =
        runTest {
            val engine =
                MockEngine { request ->
                    when (request.url.encodedPath) {
                        "/Users/AuthenticateByName" -> {
                            respond(
                                AUTHENTICATION_RESULT_JSON,
                                HttpStatusCode.OK,
                                jsonResponseHeaders,
                            )
                        }

                        "/Users/Me" -> {
                            respond(USERS_ME_JSON, HttpStatusCode.OK, jsonResponseHeaders)
                        }

                        else -> {
                            error("unexpected path: ${request.url.encodedPath}")
                        }
                    }
                }

            val me = loggedInSession(engine).me()

            assertEquals("root", me.name)
            assertEquals("2861f5b8509e46e9a6dca9a1c396501f", me.id)
            assertEquals("4a828cdb4fc0494184c5d45b616d6423", me.serverId)
            assertEquals(true, me.hasPassword)
        }

    @Test
    fun `system info parses the captured server shape`() =
        runTest {
            val engine =
                MockEngine { request ->
                    when (request.url.encodedPath) {
                        "/Users/AuthenticateByName" -> {
                            respond(
                                AUTHENTICATION_RESULT_JSON,
                                HttpStatusCode.OK,
                                jsonResponseHeaders,
                            )
                        }

                        "/System/Info" -> {
                            respond(SYSTEM_INFO_JSON, HttpStatusCode.OK, jsonResponseHeaders)
                        }

                        else -> {
                            error("unexpected path: ${request.url.encodedPath}")
                        }
                    }
                }

            val info = loggedInSession(engine).systemInfo()

            assertEquals("DLX Reference", info.serverName)
            assertEquals("12.4.0", info.version)
            assertEquals("Jellyfin Server", info.productName)
            assertEquals("4a828cdb4fc0494184c5d45b616d6423", info.id)
            assertEquals(true, info.supportsLibraryMonitor)
            assertEquals(true, info.canSelfRestart)
            assertEquals(false, info.hasPendingRestart)
        }

    @Test
    fun `session exposes the logged-in user id`() =
        runTest {
            val engine =
                MockEngine { _ ->
                    respond(AUTHENTICATION_RESULT_JSON, HttpStatusCode.OK, jsonResponseHeaders)
                }

            assertEquals("2861f5b8509e46e9a6dca9a1c396501f", loggedInSession(engine).userId)
        }
}
