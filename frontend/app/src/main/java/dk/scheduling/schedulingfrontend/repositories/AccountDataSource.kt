package dk.scheduling.schedulingfrontend.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.UUID

object AccountDataStoreKeys {
    val AUTH_TOKEN_KEY = stringPreferencesKey("AUTH_TOKEN")
}

class AccountDataSource(
    private val accountDataStore: DataStore<Preferences>,
) {
    fun retrieveAccount(): Flow<UUID> {
        return accountDataStore.data.catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { account ->
            val result = account[AccountDataStoreKeys.AUTH_TOKEN_KEY] ?: throw UserNotLoggedIN("The user is not logged in")
            UUID.fromString(result)
        }
    }

    suspend fun setAuthToken(authToken: UUID) {
        accountDataStore.edit { account ->
            account[AccountDataStoreKeys.AUTH_TOKEN_KEY] = authToken.toString()
        }
    }
}
