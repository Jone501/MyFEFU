package ru.jone501.myfefu.domain.model

import java.time.LocalDateTime
import java.util.regex.Pattern

data class Lessons(
    var lessons: List<Lesson>
)

data class Lesson(
    var id: Int,
    var start_time: LocalDateTime,
    var end_time: LocalDateTime,
    var discipline: DisciplineType,
    var ppsLoad: PpsLoadType,
    var academicSubgroup: AcademicSubgroupType?,
    var facility: FacilityType?,
)

data class DisciplineType(
    var name: String,
    var name_en: String,
)

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