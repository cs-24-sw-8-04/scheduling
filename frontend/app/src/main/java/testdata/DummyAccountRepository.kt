package testdata

import dk.scheduling.schedulingfrontend.repositories.IAccountRepository
import java.util.UUID

class DummyAccountRepository : IAccountRepository {
    override suspend fun getAuthToken(): UUID {
        return UUID.randomUUID()
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
