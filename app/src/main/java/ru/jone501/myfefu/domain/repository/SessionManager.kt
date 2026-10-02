package ru.jone501.myfefu.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.jone501.myfefu.domain.model.AuthToken

interface SessionManager {
    suspend fun set(authToken: AuthToken?)

    suspend fun get(): Flow<AuthToken?>

    suspend fun clear()
}