package dk.scheduling.schedulingfrontend.repositories

import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface IAccountRepository {
    fun getAuthToken(): Flow<UUID>

    suspend fun setAuthToken(token: UUID)

    suspend fun login(
        username: String,
        password: String,
    ): Boolean

    suspend fun signUp(
        username: String,
        password: String,
    ): Boolean
}
