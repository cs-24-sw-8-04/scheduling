package dk.scheduling.schedulingfrontend.datasources

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dk.scheduling.schedulingfrontend.exceptions.UserNotLoggedInException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.UUID

class AccountDataSource(
    context: Context,
) {
    private val Context.accountDataStore: DataStore<Preferences> by preferencesDataStore(
        name = "ACCOUNT_STORE",
    )
    private val accountDataStore = context.accountDataStore

    object AccountDataStoreKeys {
        val AUTH_TOKEN_KEY = stringPreferencesKey("AUTH_TOKEN")
        val USERNAME_KEY = stringPreferencesKey("USERNAME")
    }

    private fun getData(): Flow<Preferences> {
        return accountDataStore.data.catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
    }

    private fun getValueFromKey(key: Preferences.Key<String>): Flow<String> {
        return getData().map { account ->
            account[key] ?: throw UserNotLoggedInException("The user is not logged in!")
        }
    }

    private suspend fun setValue(
        key: Preferences.Key<String>,
        value: String,
    ) {
        accountDataStore.edit { account ->
            account[key] = value
        }
    }

    suspend fun getAuthToken(): UUID {
        try {
            val stream = getValueFromKey(AccountDataStoreKeys.AUTH_TOKEN_KEY)
            return UUID.fromString(stream.first())
        } catch (e: Throwable) {
            throw UserNotLoggedInException("The user is not logged in!", e)
        }
    }

    suspend fun setAuthToken(authToken: UUID) {
        setValue(AccountDataStoreKeys.AUTH_TOKEN_KEY, authToken.toString())
    }

    suspend fun getUsername(): String {
        try {
            val stream = getValueFromKey(AccountDataStoreKeys.USERNAME_KEY)
            return stream.first()
        } catch (e: Throwable) {
            throw UserNotLoggedInException("The user is not logged in!", e)
        }
    }

    suspend fun setUsername(username: String) {
        setValue(AccountDataStoreKeys.USERNAME_KEY, username)
    }

    suspend fun logout() {
        accountDataStore.edit { preferences ->
            preferences.remove(AccountDataStoreKeys.AUTH_TOKEN_KEY)
        }
        accountDataStore.edit { preferences ->
            preferences.remove(AccountDataStoreKeys.USERNAME_KEY)
        }
    }
}
