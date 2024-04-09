@file:Suppress("PropertyName")

package dk.scheduling.schedulingfrontend.api.protocol

import java.util.UUID

data class RegisterOrLoginRequest(
    val username: String,
    val password: String,
)

data class RegisterOrLoginResponse(
    val auth_token: UUID,
)
