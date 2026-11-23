package app.dlxplayer.shared.jellyfin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Public server details from `GET /System/Info/Public`.
 *
 * Jellyfin serializes responses with PascalCase keys (verified against a
 * Jellyfin 12.4.0 reference server), so each property pins its exact wire
 * name via [SerialName]. Optional fields default so the client keeps
 * working against servers that omit them.
 */
@Serializable
data class PublicSystemInfo(
    @SerialName("LocalAddress") val localAddress: String? = null,
    @SerialName("ServerName") val serverName: String? = null,
    @SerialName("Version") val version: String? = null,
    @SerialName("ProductName") val productName: String? = null,
    @SerialName("OperatingSystem") val operatingSystem: String? = null,
    @SerialName("Id") val id: String? = null,
    @SerialName("StartupWizardCompleted") val startupWizardCompleted: Boolean = false,
)
