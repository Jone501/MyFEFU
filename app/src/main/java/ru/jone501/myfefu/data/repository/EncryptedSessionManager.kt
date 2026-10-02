package ru.jone501.myfefu.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.jone501.myfefu.domain.model.AuthToken
import ru.jone501.myfefu.domain.repository.SessionManager

class EncryptedSessionManager(
    private val dataStore: DataStore<Preferences>,
) : SessionManager {
    override suspend fun set(authToken: AuthToken?) {
        updateData {
            val json = Json.encodeToString(authToken)
            it[AUTH_TOKEN_KEY] = json
            Log.i("SESSION", "access:  ${authToken?.accessToken}")
            Log.i("SESSION", "refresh: ${authToken?.refreshToken}")
        }
    }

    override suspend fun get(): Flow<AuthToken?> {
        return withContext(Dispatchers.IO) {
            val value = dataStore.data.map { preferences -> preferences[AUTH_TOKEN_KEY] }
            return@withContext value.let {
                it.map { x ->
                    if (x != null)
                        Json.decodeFromString<AuthToken>(x)
                    else null
                }
            }
        }
    }

    override suspend fun clear() {
        updateData {
            it.remove(AUTH_TOKEN_KEY)
        }
    }

    private suspend fun updateData(block: (MutablePreferences) -> Unit) {
        withContext(Dispatchers.IO) {
            dataStore.updateData {
                it.toMutablePreferences().also { preferences ->
                    block(preferences)
                }
            }
        }
    }

    companion object {
        val AUTH_TOKEN_KEY = stringPreferencesKey("AUTH_TOKEN_KEY")
    }
}