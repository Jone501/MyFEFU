package ru.jone501.myfefu.domain.model

import java.time.LocalDate
import java.util.UUID

class ProfileInfo(
    val id: Int,
    val userId: Int,
    val username: String,
    val isAdult: Boolean,
    val guid: UUID,
    val emain: String,
    val lastName: String,
    val middleName: String,
    val firstName: String,
    val fullName: String,
    val birthDate: LocalDate,
    val sex: String,
    val mobile: String,
    val citizenshipId: Int,
    val roles: Array<String>,
    val status: String
)