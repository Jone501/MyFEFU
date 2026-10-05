package ru.jone501.myfefu.data.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.jone501.myfefu.domain.model.Lesson
import ru.jone501.myfefu.domain.model.StudentProfileInfo
import ru.jone501.myfefu.networking.api.ApiService
import ru.jone501.myfefu.networking.api.request.ScheduleRequest
import ru.jone501.myfefu.networking.token.NetworkRequestInterceptor
import ru.jone501.myfefu.utils.academicYearStartDate
import java.io.IOException
import java.time.LocalDate

class LessonsViewModel(
    val profileInfoViewModel: ProfileInfoViewModel,
    val apiService: ApiService
) : ViewModel() {
    private val _lessons: MutableStateFlow<MutableMap<LocalDate, MutableList<Lesson>>?> =
        MutableStateFlow(null)
    private val _subgroups: MutableStateFlow<MutableSet<String>?> = MutableStateFlow(null)
    private var _requiresLogin = MutableStateFlow(false)

    val lessons = _lessons.asStateFlow()
    val subgroups = _subgroups.asStateFlow()
    val requiresLogin = _requiresLogin.asStateFlow()

    init {
        init()
    }

    fun init() {
        _requiresLogin.value = false
        _lessons.value = null
        _subgroups.value = null
        viewModelScope.launch {
            profileInfoViewModel.studentProfileInfo.collect { studentsInfo ->
                if (studentsInfo != null) {
                    Log.i("STUDENTS_INFO", studentsInfo.toString())
                    loadLessons(studentsInfo)
                }
            }
        }
    }

    private suspend fun loadLessons(studentProfileInfo: List<StudentProfileInfo>?) {
        val startOfAcademicYear = LocalDate.now().academicYearStartDate()
        try {
            apiService.getSchedule(
                ScheduleRequest(
                    """
query ReadLessons {
    lessons(
        start_time: "$startOfAcademicYear 00:00:00",
        end_time: "${startOfAcademicYear.plusYears(1)} 00:00:00",
        academic_groups: [${studentProfileInfo?.firstOrNull()?.academicGroupId}]
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
            ).let {
                if (it.isSuccessful) {
                    val lessonMap: MutableMap<LocalDate, MutableList<Lesson>> = mutableMapOf()
                    val subgroupsSet: MutableSet<String> = mutableSetOf()

                    val lessonsToAdd = it.body()?.data?.lessons?: listOf()
                    for (lesson in lessonsToAdd) {
                        val lessonDate = lesson.start_time.toLocalDate()
                        if (lessonMap[lessonDate] == null) {
                            lessonMap[lessonDate] = mutableListOf()
                        }
                        if (!lessonMap[lessonDate]!!.contains(lesson)) {
                            lessonMap[lessonDate]!!.add(lesson)
                            if (lesson.academicSubgroup != null) {
                                subgroupsSet.add(lesson.academicSubgroup!!.name)
                            }
                        }
                    }

                    Log.i("LESSONS", lessonMap.toString())
                    Log.i("SUBGROUPS", subgroupsSet.toString())

                    _lessons.value = lessonMap
                    _subgroups.value = subgroupsSet
                } else when (it.code()) {
                    NetworkRequestInterceptor.UNAUTHORIZED_CODE,
                    NetworkRequestInterceptor.FORBIDDEN_CODE -> {
                        _requiresLogin.value = true
                    }
                }
            }
        } catch (e: IOException) {
            Log.i("ERROR", e.message, e)
        }
    }
}