package app.dlxplayer.shared.jellyfin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Minimal playback capability description sent to
 * `POST /Items/{id}/PlaybackInfo`. The server compares it against the
 * item's media to decide direct play vs transcode.
 */
@Serializable
data class DeviceProfile(
    @SerialName("MaxStreamingBitrate") val maxStreamingBitrate: Long? = null,
    @SerialName("DirectPlayProfiles") val directPlayProfiles: List<DirectPlayProfile> = emptyList(),
)

/** One container the player can play directly, e.g. mp4 + Video. */
@Serializable
data class DirectPlayProfile(
    @SerialName("Container") val container: String? = null,
    @SerialName("Type") val type: String? = null,
)

/** Request body for `POST /Items/{id}/PlaybackInfo`. */
@Serializable
data class PlaybackInfoRequest(
    @SerialName("DeviceProfile") val deviceProfile: DeviceProfile? = null,
)

/** One playable stream inside an item, e.g. a video or audio track. */
@Serializable
data class MediaStreamDto(
    @SerialName("Type") val type: String? = null,
    @SerialName("Codec") val codec: String? = null,
    @SerialName("DisplayTitle") val displayTitle: String? = null,
    @SerialName("IsDefault") val isDefault: Boolean = false,
    @SerialName("Width") val width: Int? = null,
    @SerialName("Height") val height: Int? = null,
)

/** A playable version of an item, with the server's delivery decision. */
@Serializable
data class MediaSourceDto(
    @SerialName("Id") val id: String? = null,
    @SerialName("Name") val name: String? = null,
    @SerialName("Container") val container: String? = null,
    @SerialName("RunTimeTicks") val runTimeTicks: Long? = null,
    @SerialName("SupportsDirectPlay") val supportsDirectPlay: Boolean = false,
    @SerialName("SupportsDirectStream") val supportsDirectStream: Boolean = false,
    @SerialName("SupportsTranscoding") val supportsTranscoding: Boolean = false,
    @SerialName("MediaStreams") val mediaStreams: List<MediaStreamDto> = emptyList(),
)

/** Response of `POST /Items/{id}/PlaybackInfo`. */
@Serializable
data class PlaybackInfo(
    @SerialName("MediaSources") val mediaSources: List<MediaSourceDto> = emptyList(),
    @SerialName("PlaySessionId") val playSessionId: String? = null,
)
