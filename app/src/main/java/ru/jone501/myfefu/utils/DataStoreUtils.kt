package ru.jone501.myfefu.utils

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

suspend fun DataStore<Preferences>.updateData(block: (MutablePreferences) -> Unit) {
    withContext(Dispatchers.IO) {
        updateData {
            it.toMutablePreferences().also { preferences ->
                block(preferences)
            }
        }
    }
}

suspend fun DataStore<Preferences>.updateKeysData(vararg keys: Preferences.Key<*>, block: (MutablePreferences, Preferences.Key<*>) -> Unit) {
    withContext(Dispatchers.IO) {
        updateData {
            it.toMutablePreferences().also { preferences ->
                for (key in keys) { block(preferences, key) }
            }
        }
    }
}

suspend fun <T> DataStore<Preferences>.updateData(key: Preferences.Key<T>, value: T) {
    withContext(Dispatchers.IO) {
        updateData {
            it.toMutablePreferences().also { preferences ->
                preferences[key] = value
            }
        }
    }
}

suspend fun <T> DataStore<Preferences>.getData(key: Preferences.Key<T>): Flow<T?> {
    return withContext(Dispatchers.IO) {
        return@withContext data.map { preferences -> preferences[key] }
    }
}

suspend fun <T, R> DataStore<Preferences>.getData(key: Preferences.Key<T>, transform: suspend (T?) -> R): Flow<R> {
    return withContext(Dispatchers.IO) {
        return@withContext data.map { preferences -> transform(preferences[key]) }
    }
}