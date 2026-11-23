package app.dlxplayer.shared.jellyfin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A user-visible library (view), from `GET /Users/{userId}/Views`. */
@Serializable
data class LibraryDto(
    @SerialName("Name") val name: String? = null,
    @SerialName("Id") val id: String? = null,
    /** Library kind, e.g. `movies`, `tvshows`, `music`; null for mixed views. */
    @SerialName("CollectionType") val collectionType: String? = null,
)
