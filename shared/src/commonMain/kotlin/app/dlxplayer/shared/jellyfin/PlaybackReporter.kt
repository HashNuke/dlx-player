package app.dlxplayer.shared.jellyfin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Body of a playback report to `/Sessions/Playing`,
 * `/Sessions/Playing/Progress` or `/Sessions/Playing/Stopped`. All
 * three routes accept the same field set (verified live); positions are
 * Jellyfin ticks (100ns units).
 */
@Serializable
data class PlaybackReport(
    @SerialName("ItemId") val itemId: String,
    @SerialName("MediaSourceId") val mediaSourceId: String? = null,
    @SerialName("PlaySessionId") val playSessionId: String? = null,
    @SerialName("PositionTicks") val positionTicks: Long? = null,
    @SerialName("IsPaused") val isPaused: Boolean? = null,
    /** `DirectPlay`, `DirectStream` or `Transcode`. */
    @SerialName("PlayMethod") val playMethod: String? = null,
)

/**
 * Reports one playback to the server; the platform players drive it
 * from their playback callbacks. Create one per playback via
 * [JellyfinSession.playbackReporter], then call [start] when playback
 * begins, [progress] as the position advances, and [stop] when it ends.
 * The server persists the stop position, so the item shows up in
 * continue-watching with a resume point.
 */
class PlaybackReporter internal constructor(
    private val session: JellyfinSession,
    private val report: PlaybackReport,
) {
    /** Tells the server playback started at [positionTicks]. */
    suspend fun start(positionTicks: Long = 0) {
        post("Sessions/Playing", positionTicks, isPaused = false)
    }

    /** Reports the current playback position. */
    suspend fun progress(
        positionTicks: Long,
        isPaused: Boolean = false,
    ) {
        post("Sessions/Playing/Progress", positionTicks, isPaused)
    }

    /** Ends the playback; the server saves [positionTicks] as the resume point. */
    suspend fun stop(positionTicks: Long) {
        post("Sessions/Playing/Stopped", positionTicks, isPaused = false)
    }

    private suspend fun post(
        path: String,
        positionTicks: Long,
        isPaused: Boolean,
    ) {
        session.reportPlayback(
            path = path,
            report =
                report.copy(
                    positionTicks = positionTicks,
                    isPaused = isPaused,
                ),
        )
    }
}
