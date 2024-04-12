package dk.scheduling.schedulingfrontend.repositories

import dk.scheduling.schedulingfrontend.api.getApiClient
import dk.scheduling.schedulingfrontend.api.protocol.RegisterOrLoginRequest

class LoginRepository(
    private val accountDataSource: AccountDataSource,
) {
    suspend fun makeLoginRequest(request: RegisterOrLoginRequest): Boolean {
        val response = getApiClient().loginToAccount(request)
        if (response.isSuccessful) {
            accountDataSource.setAuthToken(response.body()?.auth_token ?: return false)
            return true
        }
        return false
    }
}
