package dk.scheduling.schedulingfrontend.repositories

import dk.scheduling.schedulingfrontend.api.getApiClient
import dk.scheduling.schedulingfrontend.api.protocol.RegisterOrLoginRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import java.util.UUID

class AccountRepository(
    private val accountDataSource: AccountDataSource,
) : IAccountRepository {
    override fun getAuthToken(): Flow<UUID> {
        return accountDataSource.retrieveAccount()
    }

    override suspend fun setAuthToken(token: UUID) {
        accountDataSource.setAuthToken(token)
    }

    override suspend fun login(
        username: String,
        password: String,
    ): Boolean {
        val response = getApiClient().loginToAccount(RegisterOrLoginRequest(username, password))
        if (response.isSuccessful) {
            setAuthToken(response.body()?.auth_token ?: return false)
            return true
        }
        return false
    }

    override suspend fun signUp(
        username: String,
        password: String,
    ): Boolean {
        val response = getApiClient().registerAccount(RegisterOrLoginRequest(username, password))
        if (response.isSuccessful) {
            setAuthToken(response.body()?.auth_token ?: return false)
            return true
        }
        return false
    }
}

class DummyAccountRepository : IAccountRepository {
    override fun getAuthToken(): Flow<UUID> {
        return object : Flow<UUID> {
            override suspend fun collect(collector: FlowCollector<UUID>) {
                collector.emit(UUID.randomUUID())
            }
        }
    }

    override suspend fun setAuthToken(token: UUID) {
    }

    override suspend fun login(
        username: String,
        password: String,
    ): Boolean {
        return false
    }

    override suspend fun signUp(
        username: String,
        password: String,
    ): Boolean {
        return false
    }
}
