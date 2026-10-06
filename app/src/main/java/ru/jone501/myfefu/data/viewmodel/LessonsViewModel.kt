package ru.jone501.myfefu.data.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.jone501.myfefu.domain.model.EmptyLesson
import ru.jone501.myfefu.domain.model.Lesson
import ru.jone501.myfefu.domain.model.StudentProfileInfo
import ru.jone501.myfefu.networking.api.ApiService
import ru.jone501.myfefu.networking.api.request.ScheduleRequest
import ru.jone501.myfefu.networking.token.NetworkRequestInterceptor
import ru.jone501.myfefu.utils.academicYearStartDate
import ru.jone501.myfefu.utils.mapState
import java.io.IOException
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class LessonsViewModel(
    val profileInfoViewModel: ProfileInfoViewModel,
    val apiService: ApiService
) : ViewModel() {
    private val _lessons: MutableStateFlow<Map<Int, Map<String, Map<LocalDate, List<Lesson>>>?>> =
        MutableStateFlow(mutableMapOf())
    private var _requiresLogin = MutableStateFlow(false)

    val lessons = _lessons.asStateFlow()
    val subgroups: StateFlow<Map<Int, Set<String>?>> = _lessons.asStateFlow().mapState { lessons ->
        lessons.mapValues { it.value?.keys?.filter { x -> x != ALL_SUBGROUPS_KEY }?.toSet() }
    }
    val requiresLogin = _requiresLogin.asStateFlow()

    init {
        init()
    }

    fun init() {
        _requiresLogin.value = false
        _lessons.value = mutableMapOf()
        viewModelScope.launch {
            profileInfoViewModel.studentProfileInfo.collect { studentsInfo ->
                if (studentsInfo != null) {
                    Log.i("STUDENTS_INFO", studentsInfo.toString())
                    loadLessons(studentsInfo)
                }
            }
        }
    }

    private suspend fun loadLessons(studentProfilesInfo: List<StudentProfileInfo>?) {
        val result: MutableMap<Int, MutableMap<String, MutableMap<LocalDate, MutableList<Lesson>>>?> = mutableMapOf()

        val startOfAcademicYear = LocalDate.now().academicYearStartDate()
        if (studentProfilesInfo != null) {
            for (studentProfileInfo in studentProfilesInfo) {
                try {
                    val response = apiService.getSchedule(
                        ScheduleRequest(
                            """
query ReadLessons {
    lessons(
        start_time: "$startOfAcademicYear 00:00:00",
        end_time: "${startOfAcademicYear.plusYears(1)} 00:00:00",
        academic_groups: [${studentProfileInfo.academicGroupId}]
    ) {
        id
        guid
        discipline {
            name
            name_en
        }
        start_time
        end_time
        academicGroup {
            name
        }
        facility {
            name
        }
        teacher {
            fullName
            id
            academicDegree {
                name
                name_en
            }
        }
        academicControl {
            name
            name_en
        }
        ppsLoad {
            name
            name_en
        }
        academicSubgroup {
            name
        }
        distance_education_url
        distance_education_description
    }
}
"""
                        )
                    )
                    val body = response.body()
                    if (response.isSuccessful && body != null) {
                        val groupScheduleMap = mutableMapOf<String, MutableMap<LocalDate, MutableList<Lesson>>>()
                        putGroupLessons(groupScheduleMap, body.data.lessons)
                        result[studentProfileInfo.academicGroupId] = groupScheduleMap
                    } else when (response.code()) {
                        NetworkRequestInterceptor.UNAUTHORIZED_CODE,
                        NetworkRequestInterceptor.FORBIDDEN_CODE -> {
                            _requiresLogin.value = true
                            return
                        }
                    }
                } catch (e: IOException) {
                    Log.i("ERROR", e.message, e)
                }
            }
        }
        _lessons.value = result
    }

    private fun putGroupLessons(map: MutableMap<String, MutableMap<LocalDate, MutableList<Lesson>>>, lessons: List<Lesson>) {
        map[ALL_SUBGROUPS_KEY] = mutableMapOf()
        for (lesson in lessons) {
            val subgroup = lesson.academicSubgroup
            if (subgroup != null) {
                if (!map.containsKey(subgroup.name)) {
                    map[subgroup.name] = mutableMapOf()
                }
            }
        }

        for (lesson in lessons) {
            val subgroup = lesson.academicSubgroup
            val date = lesson.start_time.toLocalDate()
            if (subgroup != null) {
                map.computeIfAbsent(subgroup.name) { mutableMapOf() }
                    .computeIfAbsent(date) { mutableListOf() }
                    .add(lesson)
            } else {
                for (subgroupMap in map.values) {
                    subgroupMap.computeIfAbsent(date) { mutableListOf() }
                        .add(lesson)
                }
            }
        }

        for ((subgroup, subgroupMap) in map.entries) {
            for (currentDateLessonEntries in subgroupMap.entries) {
                subgroupMap[currentDateLessonEntries.key] = currentDateLessonEntries.value
                    .sortedBy { it.start_time }
                    .fillWindows(subgroup)
                    .toMutableList()
            }
        }
    }

    private fun List<Lesson>.fillWindows(subgroup: String): List<Lesson> {
        val result: MutableList<Lesson> = mutableListOf()
        for ((current, next) in this.zipWithNext()) {
            result.add(current)
            var differenceInMinutes = ChronoUnit.MINUTES.between(current.end_time, next.start_time) - 10
            while (differenceInMinutes > 0) {
                result.add(EmptyLesson(next.start_time.minusMinutes(differenceInMinutes), next.start_time.minusMinutes(differenceInMinutes - 90), subgroup))
                differenceInMinutes -= 100
            }
        }
        if (!this.isEmpty())
            result.add(this.last())
        return result
    }

    companion object {
        const val ALL_SUBGROUPS_KEY = "ALL_SUBGROUPS_KEY"
    }
}