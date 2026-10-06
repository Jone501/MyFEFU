package ru.jone501.myfefu.domain.model

import java.time.LocalDateTime
import java.util.Objects
import java.util.UUID
import java.util.regex.Pattern

data class Lessons(
    var lessons: List<Lesson>
)

@Suppress("PropertyName")
open class Lesson(
    var id: Int,
    var guid: UUID,
    var discipline: DisciplineType,
    open var start_time: LocalDateTime,
    open var end_time: LocalDateTime,
    var academicGroup: AcademicGroupType,
    var facility: FacilityType?,
    var teacher: TeacherType,
    var academicControl: Any?,
    var ppsLoad: PpsLoadType,
    var academicSubgroup: AcademicSubgroupType?,
    var distance_education_url: String,
    var distance_education_description: String,
)

class EmptyLesson(
    startTime: LocalDateTime,
    endTime: LocalDateTime,
    subgroup: String?
) : Lesson(
    Objects.hash(startTime, endTime),
    UUID.randomUUID(),
    DisciplineType("WINDOW", "WINDOW"),
    startTime,
    endTime,
    AcademicGroupType(""),
    null,
    TeacherType("", "", AcademicDegreeType("", "")),
    null,
    PpsLoadType("", ""),
    if (subgroup != null) AcademicSubgroupType(subgroup) else null,
    "",
    ""
)

data class AcademicGroupType(
    val name: String
)

data class TeacherType(
    val fullName: String,
    val id: String,
    val academicDegree: AcademicDegreeType
)

@Suppress("PropertyName")
data class AcademicDegreeType(
    val name: String,
    val name_en: String,
)

@Suppress("PropertyName")
data class DisciplineType(
    var name: String,
    var name_en: String,
)

@Suppress("PropertyName")
data class PpsLoadType(
    var name: String,
    var name_en: String,
)

data class FacilityType(
    var name: String,
)

fun facilityPrettier(input: String?): String? {
    if (input == null)
        return null
    val matcher = Pattern.compile("""^(\w+)\((\w+)\)$""").matcher(input)
    return if (matcher.matches())
        "${matcher.group(1)} (${matcher.group(2)})"
    else input
}

data class AcademicSubgroupType(
    var name: String,
)