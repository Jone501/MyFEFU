package ru.jone501.myfefu.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecodingException
import ru.jone501.myfefu.domain.model.AuthToken
import ru.jone501.myfefu.domain.model.ProfileInfo
import ru.jone501.myfefu.domain.model.StudentProfileInfo
import ru.jone501.myfefu.domain.repository.SessionManager
import ru.jone501.myfefu.utils.getData
import ru.jone501.myfefu.utils.updateData
import ru.jone501.myfefu.utils.updateKeysData


class EncryptedSessionManager(
    private val dataStore: DataStore<Preferences>,
    private val gson: Gson,
) : SessionManager {
    override suspend fun setToken(authToken: AuthToken?) {
        dataStore.updateData(AUTH_TOKEN_KEY, Json.encodeToString(authToken))
    }

    override suspend fun getToken(): Flow<AuthToken?> {
        return dataStore.getData(AUTH_TOKEN_KEY) {
            if (it != null) Json.decodeFromString<AuthToken>(it)
            else null
        }
    }

    override suspend fun setProfileInfo(profileInfo: ProfileInfo?) {
        dataStore.updateData(PROFILE_INFO_KEY, gson.toJson(profileInfo))
    }

    override suspend fun getProfileInfo(): Flow<ProfileInfo?> {
        return dataStore.getData(PROFILE_INFO_KEY) {
            if (it != null) gson.fromJson(it, ProfileInfo::class.java)
            else null
        }
    }

    override suspend fun setStudentProfileInfo(studentProfileInfo: List<StudentProfileInfo?>) {
        dataStore.updateData(STUDENT_PROFILE_INFO_KEY, gson.toJson(studentProfileInfo))
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
        return dataStore.getData(STUDENT_PROFILE_INFO_KEY) {
            if (it != null) {
                val result: MutableList<StudentProfileInfo> = mutableListOf()
                val element: JsonElement? = JsonParser.parseString(it)
                if (element?.isJsonArray?: false) {
                    for (info in element.asJsonArray!!) {
                        gson.fromJson(info, StudentProfileInfo::class.java).let { y ->
                            if (y != null) result.add(y)
                        }
                    }
                }
                result
            } else null
        }
    }

    override suspend fun setSelectedSubgroup(group: Int, subgroup: String) {
        getSelectedSubgroup().firstOrNull().let { map ->
            val newMap = map?.toMutableMap()?: mutableMapOf()
            newMap[group] = subgroup
            dataStore.updateData(SELECTED_SUBGROUP_KEY, Json.encodeToString(newMap))
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun getSelectedSubgroup(): Flow<Map<Int, String>?> {
        return dataStore.getData(SELECTED_SUBGROUP_KEY) {
            if (it != null) try {
                Json.decodeFromString<Map<Int, String>>(it)
            } catch (_: JsonDecodingException) {
                return@getData null
            }
            else null
        }
    }

    override suspend fun setSelectedGroup(group: Int) {
        dataStore.updateData(SELECTED_GROUP_KEY, group)
    }

    override suspend fun getSelectedGroup(): Flow<Int?> {
        return dataStore.getData(SELECTED_GROUP_KEY)
    }

    override suspend fun clear() {
        dataStore.updateKeysData(
            AUTH_TOKEN_KEY,
            PROFILE_INFO_KEY,
            STUDENT_PROFILE_INFO_KEY,
            SELECTED_GROUP_KEY,
            SELECTED_SUBGROUP_KEY,
        ) { preferences, key -> preferences.remove(key) }
    }

    companion object {
        val AUTH_TOKEN_KEY = stringPreferencesKey("AUTH_TOKEN_KEY")
        val PROFILE_INFO_KEY = stringPreferencesKey("PROFILE_INFO_KEY")
        val STUDENT_PROFILE_INFO_KEY = stringPreferencesKey("STUDENT_PROFILE_INFO_KEY")
        val SELECTED_GROUP_KEY = intPreferencesKey("SELECTED_GROUP_KEY")
        val SELECTED_SUBGROUP_KEY = stringPreferencesKey("SELECTED_SUBGROUP_KEY")
    }
}