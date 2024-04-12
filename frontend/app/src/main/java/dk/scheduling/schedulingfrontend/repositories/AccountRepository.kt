package dk.scheduling.schedulingfrontend.repositories

import dk.scheduling.schedulingfrontend.api.protocol.RegisterOrLoginRequest
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class AccountRepository(
    private val loginRepo: LoginRepository,
    private val authRepo: AuthorizationRepository,
) {
    fun getAuthToken(): Flow<UUID> {
        return authRepo.getAuthToken()
    }

    suspend fun setAuthToken(token: UUID) {
        authRepo.setAuthToken(token)
    }

    suspend fun login(
        username: String,
        password: String,
    ): Boolean {
        return loginRepo.makeLoginRequest(RegisterOrLoginRequest(username, password))
    }
}

class AuthorizationRepository(
    private val accountDataSource: AccountDataSource,
) {
    fun getAuthToken(): Flow<UUID> {
        return accountDataSource.retrieveAccount()
    }

    suspend fun setAuthToken(token: UUID) {
        accountDataSource.setAuthToken(token)
    }
}
