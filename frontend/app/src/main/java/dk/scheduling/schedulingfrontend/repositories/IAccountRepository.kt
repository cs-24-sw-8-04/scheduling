package dk.scheduling.schedulingfrontend.repositories

import java.util.UUID

interface IAccountRepository {
    suspend fun getAuthToken(): UUID

    suspend fun login(
        username: String,
        password: String,
    ): Boolean

    suspend fun signUp(
        username: String,
        password: String,
    ): Boolean

    suspend fun getUsername(): String

    suspend fun isLoggedIn(): Boolean

    suspend fun logout()
}
