package dk.scheduling.schedulingfrontend.repositories.account

import dk.scheduling.schedulingfrontend.api.ApiService
import dk.scheduling.schedulingfrontend.api.protocol.RegisterOrLoginRequest
import dk.scheduling.schedulingfrontend.datasources.AccountDataSource
import java.util.UUID

class AccountRepository(
    private val accountDataSource: AccountDataSource,
    private val service: ApiService,
) : IAccountRepository {
    override suspend fun isLoggedIn(): Boolean {
        return try {
            getAuthToken()
            true
        } catch (e: Throwable) {
            false
        }
    }

    override suspend fun logout() {
        accountDataSource.logout()
    }

    override suspend fun getAuthToken(): UUID {
        return accountDataSource.getAuthToken()
    }

    override suspend fun getUsername(): String {
        return accountDataSource.getUsername()
    }

    override suspend fun login(
        username: String,
        password: String,
    ): Boolean {
        val response = service.loginToAccount(RegisterOrLoginRequest(username, password))
        if (response.isSuccessful) {
            val token = response.body()?.auth_token ?: return false
            accountDataSource.setAuthToken(token)
            accountDataSource.setUsername(username)
            return true
        }
        return false
    }

    override suspend fun signUp(
        username: String,
        password: String,
    ): Boolean {
        val response = service.registerAccount(RegisterOrLoginRequest(username, password))
        if (response.isSuccessful) {
            val token = response.body()?.auth_token ?: return false
            accountDataSource.setAuthToken(token)
            accountDataSource.setUsername(username)
            return true
        }
        return false
    }
}
