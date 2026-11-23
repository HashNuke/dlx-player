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
import kotlin.test.assertFalse

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

    @Test
    fun `views lists the user's libraries`() =
        runTest {
            var viewsPath: String? = null
            val engine =
                MockEngine { request ->
                    when (request.url.encodedPath) {
                        "/Users/AuthenticateByName" -> {
                            respond(AUTHENTICATION_RESULT_JSON, HttpStatusCode.OK, jsonResponseHeaders)
                        }

                        "/Users/2861f5b8509e46e9a6dca9a1c396501f/Views" -> {
                            viewsPath = request.url.encodedPath
                            respond(VIEWS_JSON, HttpStatusCode.OK, jsonResponseHeaders)
                        }

                        else -> {
                            error("unexpected path: ${request.url.encodedPath}")
                        }
                    }
                }

            val views = loggedInSession(engine).views()

            assertEquals("/Users/2861f5b8509e46e9a6dca9a1c396501f/Views", viewsPath)
            assertEquals(3, views.size)
            assertEquals("Movies", views[0].name)
            assertEquals("f137a2dd21bbc1b99aa5c0f6bf02a805", views[0].id)
            assertEquals("movies", views[0].collectionType)
            assertEquals("Music", views[1].name)
            assertEquals("music", views[1].collectionType)
            assertEquals("Shows", views[2].name)
            assertEquals("tvshows", views[2].collectionType)
        }

    @Test
    fun `paged items query the library and parse the captured shape`() =
        runTest {
            var itemsPath: String? = null
            var parentId: String? = null
            var startIndex: String? = null
            var limit: String? = null
            var sortBy: String? = null
            var fields: String? = null
            var mediaTypes: String? = null
            val engine =
                MockEngine { request ->
                    when (request.url.encodedPath) {
                        "/Users/AuthenticateByName" -> {
                            respond(AUTHENTICATION_RESULT_JSON, HttpStatusCode.OK, jsonResponseHeaders)
                        }

                        "/Users/2861f5b8509e46e9a6dca9a1c396501f/Items" -> {
                            itemsPath = request.url.encodedPath
                            parentId = request.url.parameters["parentId"]
                            startIndex = request.url.parameters["startIndex"]
                            limit = request.url.parameters["limit"]
                            sortBy = request.url.parameters["sortBy"]
                            fields = request.url.parameters["fields"]
                            mediaTypes = request.url.parameters["mediaTypes"]
                            respond(ITEMS_JSON, HttpStatusCode.OK, jsonResponseHeaders)
                        }

                        else -> {
                            error("unexpected path: ${request.url.encodedPath}")
                        }
                    }
                }

            val page =
                loggedInSession(engine).items(
                    ItemsQuery(
                        parentId = "f137a2dd21bbc1b99aa5c0f6bf02a805",
                        startIndex = 0,
                        limit = 20,
                        sortBy = "SortName",
                        fields = listOf("Overview", "Genres"),
                        mediaTypes = listOf("Video"),
                    ),
                )

            assertEquals("/Users/2861f5b8509e46e9a6dca9a1c396501f/Items", itemsPath)
            assertEquals("f137a2dd21bbc1b99aa5c0f6bf02a805", parentId)
            assertEquals("0", startIndex)
            assertEquals("20", limit)
            assertEquals("SortName", sortBy)
            assertEquals("Overview,Genres", fields)
            assertEquals("Video", mediaTypes)

            assertEquals(1, page.totalRecordCount)
            assertEquals(1, page.items.size)
            val movie = page.items[0]
            assertEquals("Sample Movie", movie.name)
            assertEquals("a1d6b5566e7586a2ff99614f1950c129", movie.id)
            assertEquals("Movie", movie.type)
            assertEquals(2026, movie.productionYear)
            assertEquals(600000000L, movie.runTimeTicks)
            assertEquals("97f8cfef56c10461f535af44a7bcc136", movie.imageTags["Primary"])
            assertEquals(0L, movie.userData?.playbackPositionTicks)
            assertEquals(false, movie.userData?.played)
            assertEquals(false, movie.userData?.isFavorite)
        }

    @Test
    fun `image urls carry image type sizing and api key`() =
        runTest {
            val engine =
                MockEngine { _ ->
                    respond(AUTHENTICATION_RESULT_JSON, HttpStatusCode.OK, jsonResponseHeaders)
                }
            val session = loggedInSession(engine)

            assertEquals(
                "http://192.168.1.10:8096/Items/a1d6b5566e7586a2ff99614f1950c129/Images/Primary" +
                    "?api_key=f6547397831d44718e44f89bc0d66ced&maxWidth=100&maxHeight=56",
                session.imageUrl(
                    itemId = "a1d6b5566e7586a2ff99614f1950c129",
                    imageType = "Primary",
                    maxWidth = 100,
                    maxHeight = 56,
                ),
            )
            assertEquals(
                "http://192.168.1.10:8096/Items/a1d6b5566e7586a2ff99614f1950c129/Images/Backdrop" +
                    "?api_key=f6547397831d44718e44f89bc0d66ced",
                session.imageUrl(itemId = "a1d6b5566e7586a2ff99614f1950c129", imageType = "Backdrop"),
            )
        }

    @Test
    fun `resume items carry playback position and percentage`() =
        runTest {
            var resumePath: String? = null
            var resumeMediaTypes: String? = null
            val engine =
                MockEngine { request ->
                    when (request.url.encodedPath) {
                        "/Users/AuthenticateByName" -> {
                            respond(AUTHENTICATION_RESULT_JSON, HttpStatusCode.OK, jsonResponseHeaders)
                        }

                        "/Users/2861f5b8509e46e9a6dca9a1c396501f/Items/Resume" -> {
                            resumePath = request.url.encodedPath
                            resumeMediaTypes = request.url.parameters["mediaTypes"]
                            respond(RESUME_JSON, HttpStatusCode.OK, jsonResponseHeaders)
                        }

                        else -> {
                            error("unexpected path: ${request.url.encodedPath}")
                        }
                    }
                }

            val page = loggedInSession(engine).resumeItems()

            assertEquals("/Users/2861f5b8509e46e9a6dca9a1c396501f/Items/Resume", resumePath)
            assertEquals("Video", resumeMediaTypes)
            assertEquals(1, page.totalRecordCount)
            val item = page.items[0]
            assertEquals("Sample Movie", item.name)
            assertEquals("Movie", item.type)
            assertEquals(1200000000L, item.userData?.playbackPositionTicks)
            assertEquals(30.0, item.userData?.playedPercentage)
            assertEquals(false, item.userData?.played)
        }

    @Test
    fun `next up returns the show episode to watch next`() =
        runTest {
            var nextUpPath: String? = null
            var nextUpUserId: String? = null
            val engine =
                MockEngine { request ->
                    when (request.url.encodedPath) {
                        "/Users/AuthenticateByName" -> {
                            respond(AUTHENTICATION_RESULT_JSON, HttpStatusCode.OK, jsonResponseHeaders)
                        }

                        "/Shows/NextUp" -> {
                            nextUpPath = request.url.encodedPath
                            nextUpUserId = request.url.parameters["UserId"]
                            respond(NEXTUP_JSON, HttpStatusCode.OK, jsonResponseHeaders)
                        }

                        else -> {
                            error("unexpected path: ${request.url.encodedPath}")
                        }
                    }
                }

            val page = loggedInSession(engine).nextUp()

            assertEquals("/Shows/NextUp", nextUpPath)
            assertEquals("2861f5b8509e46e9a6dca9a1c396501f", nextUpUserId)
            assertEquals(1, page.totalRecordCount)
            val episode = page.items[0]
            assertEquals("Demo Show S01E01 - Episode 1", episode.name)
            assertEquals("Episode", episode.type)
            assertEquals("Demo Show", episode.seriesName)
            assertEquals(600000000L, episode.userData?.playbackPositionTicks)
        }

    @Test
    fun `favorites filter and search term reuse the items query`() =
        runTest {
            var filters: String? = null
            var searchTerm: String? = null
            val engine =
                MockEngine { request ->
                    when (request.url.encodedPath) {
                        "/Users/AuthenticateByName" -> {
                            respond(AUTHENTICATION_RESULT_JSON, HttpStatusCode.OK, jsonResponseHeaders)
                        }

                        "/Users/2861f5b8509e46e9a6dca9a1c396501f/Items" -> {
                            filters = request.url.parameters["Filters"]
                            searchTerm = request.url.parameters["searchTerm"]
                            respond(ITEMS_JSON, HttpStatusCode.OK, jsonResponseHeaders)
                        }

                        else -> {
                            error("unexpected path: ${request.url.encodedPath}")
                        }
                    }
                }
            val session = loggedInSession(engine)

            session.items(ItemsQuery(filters = listOf("IsFavorite")))
            assertEquals("IsFavorite", filters)
            assertEquals(null, searchTerm)

            session.items(ItemsQuery(searchTerm = "Sample"))
            assertEquals(null, filters)
            assertEquals("Sample", searchTerm)

            session.items(ItemsQuery(searchTerm = "Sample", filters = listOf("IsFavorite", "IsUnplayed")))
            assertEquals("IsFavorite,IsUnplayed", filters)
            assertEquals("Sample", searchTerm)
        }

    @Test
    fun `seasons and episodes parse parent child relations`() =
        runTest {
            var seasonsUserId: String? = null
            var episodesUserId: String? = null
            val seriesId = "997a914658183ac33bb8e7b24f7129f7"
            val engine =
                MockEngine { request ->
                    when (request.url.encodedPath) {
                        "/Users/AuthenticateByName" -> {
                            respond(AUTHENTICATION_RESULT_JSON, HttpStatusCode.OK, jsonResponseHeaders)
                        }

                        "/Shows/$seriesId/Seasons" -> {
                            seasonsUserId = request.url.parameters["UserId"]
                            respond(SEASONS_JSON, HttpStatusCode.OK, jsonResponseHeaders)
                        }

                        "/Shows/$seriesId/Episodes" -> {
                            episodesUserId = request.url.parameters["UserId"]
                            respond(EPISODES_JSON, HttpStatusCode.OK, jsonResponseHeaders)
                        }

                        else -> {
                            error("unexpected path: ${request.url.encodedPath}")
                        }
                    }
                }
            val session = loggedInSession(engine)

            val seasons = session.seasons(seriesId)
            assertEquals("2861f5b8509e46e9a6dca9a1c396501f", seasonsUserId)
            assertEquals(1, seasons.totalRecordCount)
            val season = seasons.items[0]
            assertEquals("Season 1", season.name)
            assertEquals("Season", season.type)
            assertEquals(1, season.indexNumber)
            assertEquals(seriesId, season.seriesId)

            val episodes = session.episodes(seriesId)
            assertEquals("2861f5b8509e46e9a6dca9a1c396501f", episodesUserId)
            assertEquals(3, episodes.totalRecordCount)
            val first = episodes.items[0]
            assertEquals("Demo Show S01E01 - Episode 1", first.name)
            assertEquals("Episode", first.type)
            assertEquals("2cfd607d4da7e741c01adc926ef8f7b0", first.seasonId)
            assertEquals("Season 1", first.seasonName)
            assertEquals(seriesId, first.seriesId)
            assertEquals(1, first.indexNumber)
            assertEquals(1, first.parentIndexNumber)
            assertEquals(600000000L, first.userData?.playbackPositionTicks)
            assertEquals(3, episodes.items[2].indexNumber)
        }

    @Test
    fun `playback info posts a device profile and parses media sources`() =
        runTest {
            var playbackPath: String? = null
            var playbackMethod: HttpMethod? = null
            var playbackBody: String? = null
            val engine =
                MockEngine { request ->
                    when (request.url.encodedPath) {
                        "/Users/AuthenticateByName" -> {
                            respond(AUTHENTICATION_RESULT_JSON, HttpStatusCode.OK, jsonResponseHeaders)
                        }

                        "/Items/a1d6b5566e7586a2ff99614f1950c129/PlaybackInfo" -> {
                            playbackPath = request.url.encodedPath
                            playbackMethod = request.method
                            playbackBody = request.body.toByteArray().decodeToString()
                            respond(PLAYBACK_INFO_JSON, HttpStatusCode.OK, jsonResponseHeaders)
                        }

                        else -> {
                            error("unexpected path: ${request.url.encodedPath}")
                        }
                    }
                }

            val info = loggedInSession(engine).playbackInfo("a1d6b5566e7586a2ff99614f1950c129")

            assertEquals("/Items/a1d6b5566e7586a2ff99614f1950c129/PlaybackInfo", playbackPath)
            assertEquals(HttpMethod.Post, playbackMethod)
            assertEquals(
                """{"DeviceProfile":{"MaxStreamingBitrate":120000000,"DirectPlayProfiles":""" +
                    """[{"Container":"mp4","Type":"Video"}]}}""",
                playbackBody,
            )
            assertEquals("d70b42474a2040a4a2d92aecb708a581", info.playSessionId)
            assertEquals(1, info.mediaSources.size)
            val source = info.mediaSources[0]
            assertEquals("a1d6b5566e7586a2ff99614f1950c129", source.id)
            assertEquals("mp4", source.container)
            assertEquals(4000000000L, source.runTimeTicks)
            assertEquals(true, source.supportsDirectPlay)
            assertEquals(true, source.supportsDirectStream)
            assertEquals(false, source.supportsTranscoding)
            assertEquals(2, source.mediaStreams.size)
            assertEquals("Video", source.mediaStreams[0].type)
            assertEquals("h264", source.mediaStreams[0].codec)
            assertEquals("Audio", source.mediaStreams[1].type)
            assertEquals("aac", source.mediaStreams[1].codec)
        }

    @Test
    fun `stream url builders cover progressive hls and universal audio`() =
        runTest {
            val engine =
                MockEngine { _ ->
                    respond(AUTHENTICATION_RESULT_JSON, HttpStatusCode.OK, jsonResponseHeaders)
                }
            val session = loggedInSession(engine)
            val itemId = "a1d6b5566e7586a2ff99614f1950c129"

            assertEquals(
                "http://192.168.1.10:8096/Videos/$itemId/stream.mp4" +
                    "?ApiKey=f6547397831d44718e44f89bc0d66ced",
                session.videoStreamUrl(itemId),
            )
            assertEquals(
                "http://192.168.1.10:8096/Videos/$itemId/master.m3u8" +
                    "?MediaSourceId=$itemId&ApiKey=f6547397831d44718e44f89bc0d66ced",
                session.hlsMasterPlaylistUrl(itemId),
            )
            assertEquals(
                "http://192.168.1.10:8096/Videos/$itemId/hls1/main/0.ts" +
                    "?MediaSourceId=$itemId&ApiKey=f6547397831d44718e44f89bc0d66ced" +
                    "&runtimeTicks=0&actualSegmentLengthTicks=60000000",
                session.hlsSegmentUrl(itemId = itemId, segmentId = 0, actualSegmentLengthTicks = 60_000_000L),
            )
            assertEquals(
                "http://192.168.1.10:8096/Audio/717297981e0e89b3d6babdf1c95f7559/universal" +
                    "?DeviceId=device-1&UserId=2861f5b8509e46e9a6dca9a1c396501f&container=mp3" +
                    "&ApiKey=f6547397831d44718e44f89bc0d66ced",
                session.audioUniversalUrl(itemId = "717297981e0e89b3d6babdf1c95f7559"),
            )
        }

    @Test
    fun `reporter sends start progress and stop with position and method`() =
        runTest {
            val reports = mutableListOf<Pair<String, String>>()
            val engine =
                MockEngine { request ->
                    when (request.url.encodedPath) {
                        "/Users/AuthenticateByName" -> {
                            respond(AUTHENTICATION_RESULT_JSON, HttpStatusCode.OK, jsonResponseHeaders)
                        }

                        "/Sessions/Playing",
                        "/Sessions/Playing/Progress",
                        "/Sessions/Playing/Stopped",
                        -> {
                            reports += request.url.encodedPath to request.body.toByteArray().decodeToString()
                            respond(content = "", status = HttpStatusCode.NoContent)
                        }

                        else -> {
                            error("unexpected path: ${request.url.encodedPath}")
                        }
                    }
                }
            val session = loggedInSession(engine)

            val reporter =
                session.playbackReporter(
                    itemId = "a1d6b5566e7586a2ff99614f1950c129",
                    playSessionId = "d70b42474a2040a4a2d92aecb708a581",
                )
            reporter.start(positionTicks = 0)
            reporter.progress(positionTicks = 600_000_000)
            reporter.stop(positionTicks = 600_000_000)

            assertEquals(
                listOf("/Sessions/Playing", "/Sessions/Playing/Progress", "/Sessions/Playing/Stopped"),
                reports.map { it.first },
            )
            val itemId = "a1d6b5566e7586a2ff99614f1950c129"
            val playSessionId = "d70b42474a2040a4a2d92aecb708a581"
            assertEquals(
                """{"ItemId":"$itemId","MediaSourceId":"$itemId","PlaySessionId":"$playSessionId",""" +
                    """"PositionTicks":0,"IsPaused":false,"PlayMethod":"DirectPlay"}""",
                reports[0].second,
            )
            assertEquals(
                """{"ItemId":"$itemId","MediaSourceId":"$itemId","PlaySessionId":"$playSessionId",""" +
                    """"PositionTicks":600000000,"IsPaused":false,"PlayMethod":"DirectPlay"}""",
                reports[1].second,
            )
            assertEquals(
                """{"ItemId":"$itemId","MediaSourceId":"$itemId","PlaySessionId":"$playSessionId",""" +
                    """"PositionTicks":600000000,"IsPaused":false,"PlayMethod":"DirectPlay"}""",
                reports[2].second,
            )
        }

    @Test
    fun `logout posts to the sessions logout route with the token`() =
        runTest {
            var logoutMethod: HttpMethod? = null
            var logoutPath: String? = null
            var logoutAuthorizationHeader: String? = null
            val engine =
                MockEngine { request ->
                    when (request.url.encodedPath) {
                        "/Users/AuthenticateByName" -> {
                            respond(AUTHENTICATION_RESULT_JSON, HttpStatusCode.OK, jsonResponseHeaders)
                        }

                        "/Sessions/Logout" -> {
                            logoutMethod = request.method
                            logoutPath = request.url.encodedPath
                            logoutAuthorizationHeader = request.headers[HttpHeaders.Authorization]
                            respond(content = "", status = HttpStatusCode.NoContent)
                        }

                        else -> {
                            error("unexpected path: ${request.url.encodedPath}")
                        }
                    }
                }

            loggedInSession(engine).logout()

            assertEquals(HttpMethod.Post, logoutMethod)
            assertEquals("/Sessions/Logout", logoutPath)
            assertEquals(
                """MediaBrowser Client="DLX Player", Device="Pixel 9", DeviceId="device-1", """ +
                    """Version="0.1.0", Token="f6547397831d44718e44f89bc0d66ced"""",
                logoutAuthorizationHeader,
            )
        }

    @Test
    fun `mid-session unauthorized surfaces as session expired`() =
        runTest {
            val engine =
                MockEngine { request ->
                    when (request.url.encodedPath) {
                        "/Users/AuthenticateByName" -> {
                            respond(AUTHENTICATION_RESULT_JSON, HttpStatusCode.OK, jsonResponseHeaders)
                        }

                        "/Users/Me" -> {
                            respond(
                                content = "Error processing request.",
                                status = HttpStatusCode.Unauthorized,
                                headers = textResponseHeaders,
                            )
                        }

                        else -> {
                            error("unexpected path: ${request.url.encodedPath}")
                        }
                    }
                }

            val error = assertFailsWith<JellyfinSessionExpiredException> { loggedInSession(engine).me() }
            assertEquals(HttpStatusCode.Unauthorized, error.statusCode)
        }

    @Test
    fun `a bad login is not reported as session expired`() =
        runTest {
            val engine =
                MockEngine {
                    respond(
                        content = "Error processing request.",
                        status = HttpStatusCode.Unauthorized,
                        headers = textResponseHeaders,
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
                    client.authenticateByName(
                        username = "root",
                        password = "wrong",
                    )
                }

            assertEquals(HttpStatusCode.Unauthorized, error.statusCode)
            assertFalse(error is JellyfinSessionExpiredException)
        }

    private val textResponseHeaders =
        headersOf(HttpHeaders.ContentType to listOf(ContentType.Text.Plain.toString()))
}
