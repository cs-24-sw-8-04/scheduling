package testdata

import dk.scheduling.schedulingfrontend.repositories.account.IAccountRepository
import java.util.UUID

class DummyAccountRepository : IAccountRepository {
    override suspend fun getAuthToken(): UUID {
        return UUID.fromString("1619fd8e-e393-4b93-bca5-c36ed1bab15c")
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

    override suspend fun getUsername(): String {
        return "Test"
    }

    override suspend fun isLoggedIn(): Boolean {
        return false
    }

    override suspend fun logout() {
    }
}
