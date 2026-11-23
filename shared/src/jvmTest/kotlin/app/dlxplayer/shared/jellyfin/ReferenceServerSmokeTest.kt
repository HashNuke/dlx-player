package app.dlxplayer.shared.jellyfin

import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Manual verification against the local Jellyfin reference server
 * (scripts/reference-jellyfin.sh). NOT part of the automated suite: the
 * check returns immediately unless the reference URL was passed in, so CI
 * and normal `jvmTest` runs never touch a real server.
 *
 * Run it deliberately after booting the reference server:
 *
 *     sh scripts/reference-jellyfin.sh start
 *     ./gradlew :shared:jvmTest --tests '*ReferenceServerSmokeTest*' \
 *         -PjellyfinRefUrl=http://127.0.0.1:8096
 */
class ReferenceServerSmokeTest {
    private val refUrl: String? = System.getenv("JELLYFIN_REF_URL")?.takeIf { it.isNotBlank() }

    @Test
    fun `client talks to the live reference server`() =
        runTest {
            if (refUrl == null) return@runTest

            val client =
                JellyfinClient(
                    baseUrl = refUrl,
                    device =
                        DeviceInfo(
                            clientName = "DLX Player",
                            deviceName = "Dev Smoke",
                            deviceId = "smoke-1",
                            version = "0.1.0",
                        ),
                )

            val info = client.publicSystemInfo()
            assertEquals("DLX Reference", info.serverName)
            assertTrue(info.version!!.isNotBlank(), "server should report a version")
            assertTrue(info.startupWizardCompleted, "reference server should be set up")

            val password = System.getenv("JELLYFIN_REF_PASSWORD") ?: "dlx-test-pass"
            val auth = client.authenticateByName(username = "root", password = password)
            assertNotNull(auth.accessToken, "login should issue an access token")
            assertEquals("root", auth.user?.name)
        }

    @Test
    fun `session answers who am i and logout expires the token`() =
        runTest {
            if (refUrl == null) return@runTest

            val client =
                JellyfinClient(
                    baseUrl = refUrl,
                    device =
                        DeviceInfo(
                            clientName = "DLX Player",
                            deviceName = "Dev Smoke",
                            deviceId = "smoke-1",
                            version = "0.1.0",
                        ),
                )
            val password = System.getenv("JELLYFIN_REF_PASSWORD") ?: "dlx-test-pass"
            val session = client.session(client.authenticateByName(username = "root", password = password))

            val me = session.me()
            assertEquals("root", me.name)
            assertEquals(session.userId, me.id)

            val info = session.systemInfo()
            assertEquals("DLX Reference", info.serverName)
            assertTrue(info.version!!.isNotBlank(), "authenticated system info should report a version")

            session.logout()
            val expired = assertFailsWith<JellyfinSessionExpiredException> { session.me() }
            assertEquals(HttpStatusCode.Unauthorized, expired.statusCode)
        }
}
