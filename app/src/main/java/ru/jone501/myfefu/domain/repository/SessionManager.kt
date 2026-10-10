package ru.jone501.myfefu.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.jone501.myfefu.domain.model.AuthToken
import ru.jone501.myfefu.domain.model.ProfileInfo
import ru.jone501.myfefu.domain.model.StudentProfileInfo

interface SessionManager {
    suspend fun setToken(authToken: AuthToken?)

    suspend fun getToken(): Flow<AuthToken?>

    suspend fun setProfileInfo(profileInfo: ProfileInfo?)

    suspend fun getProfileInfo(): Flow<ProfileInfo?>

    suspend fun setStudentProfileInfo(studentProfileInfo: List<StudentProfileInfo?>)

    suspend fun addStudentProfileInfo(studentProfileInfo: StudentProfileInfo?): Boolean

    suspend fun getStudentProfileInfo(): Flow<List<StudentProfileInfo>?>

    suspend fun setSelectedSubgroup(group: Int, subgroup: String)

    suspend fun getSelectedSubgroup(): Flow<Map<Int, String>?>

    suspend fun setSelectedGroup(group: Int)

    suspend fun getSelectedGroup(): Flow<Int?>

    suspend fun clear()
}