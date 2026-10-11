package ru.jone501.myfefu.data.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ru.jone501.myfefu.utils.getData
import ru.jone501.myfefu.utils.updateData
import ru.jone501.myfefu.utils.updateKeysData

class AppPreferences(
    val dataStore: DataStore<Preferences>
) : ViewModel() {
    val windowsEnabled = MutableStateFlow(getDefault(SETTINGS_WINDOWS_ENABLED))

    init {
        loadWindowsEnabled()
    }

    private fun <T> getDefault(key: Preferences.Key<T>): T {
        @Suppress("UNCHECKED_CAST")
        return when (key) {
            SETTINGS_WINDOWS_ENABLED -> true
            else -> null
        } as T
    }

    private fun loadWindowsEnabled() {
        viewModelScope.launch {
            dataStore.getData(SETTINGS_WINDOWS_ENABLED) {
                return@getData if (it == null) {
                    val default = getDefault(SETTINGS_WINDOWS_ENABLED)
                    dataStore.updateData(SETTINGS_WINDOWS_ENABLED, default)
                    default
                } else it
            }.collect { windowsEnabled.value = it }
        }
    }

    fun setWindowsEnabled(windowsEnabled: Boolean) {
        if (this.windowsEnabled.value != windowsEnabled) {
            viewModelScope.launch {
                dataStore.updateData(SETTINGS_WINDOWS_ENABLED, windowsEnabled)
            }
        }
    }

    fun clear() {
        viewModelScope.launch {
            dataStore.updateKeysData(SETTINGS_WINDOWS_ENABLED)
            { preferences, key -> preferences.remove(key) }
        }
    }

    companion object {
        val SETTINGS_WINDOWS_ENABLED = booleanPreferencesKey("SETTINGS_WINDOWS_ENABLED")
    }
}