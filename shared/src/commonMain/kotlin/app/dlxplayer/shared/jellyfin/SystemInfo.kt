package app.dlxplayer.shared.jellyfin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Server details from `GET /System/Info`, the authenticated counterpart
 * of [PublicSystemInfo]. Captured from a Jellyfin 12.4.0 reference
 * server; unknown keys are ignored so the client keeps working against
 * servers that add fields.
 */
@Serializable
data class SystemInfo(
    @SerialName("LocalAddress") val localAddress: String? = null,
    @SerialName("ServerName") val serverName: String? = null,
    @SerialName("Version") val version: String? = null,
    @SerialName("ProductName") val productName: String? = null,
    @SerialName("OperatingSystem") val operatingSystem: String? = null,
    @SerialName("Id") val id: String? = null,
    @SerialName("StartupWizardCompleted") val startupWizardCompleted: Boolean = false,
    @SerialName("HasPendingRestart") val hasPendingRestart: Boolean = false,
    @SerialName("HasUpdateAvailable") val hasUpdateAvailable: Boolean = false,
    @SerialName("CanSelfRestart") val canSelfRestart: Boolean = false,
    @SerialName("SupportsLibraryMonitor") val supportsLibraryMonitor: Boolean = false,
)
