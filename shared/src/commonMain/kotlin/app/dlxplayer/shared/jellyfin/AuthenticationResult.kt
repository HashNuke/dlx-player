package app.dlxplayer.shared.jellyfin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Request body for `POST /Users/AuthenticateByName`. */
@Serializable
data class AuthenticateByNameRequest(
    @SerialName("Username") val username: String,
    @SerialName("Pw") val password: String,
)

/** Authenticated user details as returned by the server. */
@Serializable
data class UserDto(
    @SerialName("Name") val name: String? = null,
    @SerialName("ServerId") val serverId: String? = null,
    @SerialName("Id") val id: String? = null,
    @SerialName("HasPassword") val hasPassword: Boolean = false,
)

/** Result of `POST /Users/AuthenticateByName`. */
@Serializable
data class AuthenticationResult(
    @SerialName("User") val user: UserDto? = null,
    @SerialName("AccessToken") val accessToken: String? = null,
    @SerialName("ServerId") val serverId: String? = null,
)
