package ru.jone501.myfefu.domain.model

import java.util.UUID

class StudentProfileInfo(
    val id: Int,
    val guid: UUID,
    val userProfileId: Int,
    val entranceYear: String,
    val endYear: String,
    val isArchive: Int,
    val academicGroupId: Int,
    val academicGroup: String,
    val educationProgram: String,
    val department: String,
    val course: String,
    val studentStatus: String,
    val compensationType: String,
)