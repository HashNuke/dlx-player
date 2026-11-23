package app.dlxplayer.shared.jellyfin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Typed query for `GET /Users/{userId}/Items`. Only set parameters are
 * sent. Reused across library browsing, favorites and search.
 */
data class ItemsQuery(
    val parentId: String? = null,
    val startIndex: Int? = null,
    val limit: Int? = null,
    val sortBy: String? = null,
    /** Extra item fields to request, e.g. `Overview`, `Genres`. */
    val fields: List<String> = emptyList(),
    /** Media type filter, e.g. `Video`, `Audio`. */
    val mediaTypes: List<String> = emptyList(),
    /** Item filters, e.g. `IsFavorite`, `IsUnplayed` (verified live). */
    val filters: List<String> = emptyList(),
    /** Free-text search over item names (verified live). */
    val searchTerm: String? = null,
) {
    internal fun toParameters(): List<Pair<String, String>> =
        buildList {
            parentId?.let { add("parentId" to it) }
            startIndex?.let { add("startIndex" to it.toString()) }
            limit?.let { add("limit" to it.toString()) }
            sortBy?.let { add("sortBy" to it) }
            if (fields.isNotEmpty()) add("fields" to fields.joinToString(","))
            if (mediaTypes.isNotEmpty()) add("mediaTypes" to mediaTypes.joinToString(","))
            if (filters.isNotEmpty()) add("Filters" to filters.joinToString(","))
            searchTerm?.let { add("searchTerm" to it) }
        }
}

/** Per-user playback state attached to an item. */
@Serializable
data class UserDataDto(
    @SerialName("PlaybackPositionTicks") val playbackPositionTicks: Long = 0,
    @SerialName("PlayCount") val playCount: Int = 0,
    @SerialName("IsFavorite") val isFavorite: Boolean = false,
    @SerialName("Played") val played: Boolean = false,
    /** How far playback got, in percent of the runtime; only in some listings. */
    @SerialName("PlayedPercentage") val playedPercentage: Double? = null,
)

/**
 * The item subset the client consumes: identity, kind, year, runtime,
 * user state and image tags. Jellyfin items carry far more fields; the
 * unknown ones are ignored.
 */
@Serializable
data class ItemDto(
    @SerialName("Name") val name: String? = null,
    @SerialName("Id") val id: String? = null,
    /** Item kind, e.g. `Movie`, `Series`, `Episode`, `Audio`. */
    @SerialName("Type") val type: String? = null,
    @SerialName("ProductionYear") val productionYear: Int? = null,
    @SerialName("SeriesName") val seriesName: String? = null,
    /** Runtime in Jellyfin ticks (100ns units). */
    @SerialName("RunTimeTicks") val runTimeTicks: Long? = null,
    @SerialName("UserData") val userData: UserDataDto? = null,
    /** Available image types to tag, e.g. `Primary` -> tag hash. */
    @SerialName("ImageTags") val imageTags: Map<String, String> = emptyMap(),
)
