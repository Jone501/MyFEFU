package ru.jone501.myfefu.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import ru.jone501.myfefu.domain.model.AuthToken
import ru.jone501.myfefu.domain.model.ProfileInfo
import ru.jone501.myfefu.domain.model.StudentProfileInfo
import ru.jone501.myfefu.domain.repository.SessionManager


class EncryptedSessionManager(
    private val dataStore: DataStore<Preferences>,
    private val gson: Gson,
) : SessionManager {
    override suspend fun setToken(authToken: AuthToken?) {
        updateData {
            val json = Json.encodeToString(authToken)
            it[AUTH_TOKEN_KEY] = json
        }
    }

    override suspend fun getToken(): Flow<AuthToken?> {
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

    override suspend fun setProfileInfo(profileInfo: ProfileInfo?) {
        updateData {
            val json = gson.toJson(profileInfo)
            Log.i("SAVE_PROFILE", json)
            it[PROFILE_INFO_KEY] = json
        }
    }

    override suspend fun getProfileInfo(): Flow<ProfileInfo?> {
        return withContext(Dispatchers.IO) {
            val value = dataStore.data.map { preferences -> preferences[PROFILE_INFO_KEY] }
            return@withContext value.let {
                it.map { x ->
                    if (x != null)
                        gson.fromJson(x, ProfileInfo::class.java)
                    else null
                }
            }
        }
    }

    override suspend fun setStudentProfileInfo(studentProfileInfo: List<StudentProfileInfo?>) {
        updateData {
            val json = gson.toJson(studentProfileInfo)
            Log.i("SAVE_STUDENT", json)
            it[STUDENT_PROFILE_INFO_KEY] = json
        }
    }

    override suspend fun addStudentProfileInfo(studentProfileInfo: StudentProfileInfo?): Boolean {
        if (studentProfileInfo == null)
            return false
        getStudentProfileInfo().firstOrNull().let {
            if (it == null || it.count { x -> x.id == studentProfileInfo.id } == 0) {
                val newList = it?.toMutableList() ?: mutableListOf()
                newList.add(studentProfileInfo)
                setStudentProfileInfo(newList)
                return true
            }
        }
        return false
    }

    override suspend fun getStudentProfileInfo(): Flow<List<StudentProfileInfo>?> {
        return withContext(Dispatchers.IO) {
            val value = dataStore.data.map { preferences -> preferences[STUDENT_PROFILE_INFO_KEY] }
            return@withContext value.let {
                it.map { x ->
                    if (x != null) {
                        val result: MutableList<StudentProfileInfo> = mutableListOf()
                        val element: JsonElement? = JsonParser.parseString(x)
                        if (element?.isJsonArray?: false) {
                            for (info in element.asJsonArray!!) {
                                gson.fromJson(info, StudentProfileInfo::class.java).let { y ->
                                    if (y != null)
                                        result.add(y)
                                }
                            }
                        }
                        result
                    } else null
                }
            }
        }
    }

    override suspend fun setSelectedSubgroup(subgroup: String) {
        updateData {
            Log.i("SAVE_SELECTED_SUBGROUP", subgroup)
            it[SELECTED_SUBGROUP_KEY] = subgroup
        }
    }

    override suspend fun getSelectedSubgroup(): Flow<String?> {
        return withContext(Dispatchers.IO) {
            return@withContext dataStore.data.map { preferences -> preferences[SELECTED_SUBGROUP_KEY] }
        }
    }

    override suspend fun clear() {
        updateData {
            it.remove(AUTH_TOKEN_KEY)
            it.remove(PROFILE_INFO_KEY)
            it.remove(STUDENT_PROFILE_INFO_KEY)
            it.remove(SELECTED_SUBGROUP_KEY)
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
        val PROFILE_INFO_KEY = stringPreferencesKey("PROFILE_INFO_KEY")
        val STUDENT_PROFILE_INFO_KEY = stringPreferencesKey("STUDENT_PROFILE_INFO_KEY")
        val SELECTED_SUBGROUP_KEY = stringPreferencesKey("SELECTED_SUBGROUP_KEY")
    }
}