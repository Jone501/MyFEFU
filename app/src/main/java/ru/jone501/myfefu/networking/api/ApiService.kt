package ru.jone501.myfefu.networking.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import ru.jone501.myfefu.domain.model.Lessons
import ru.jone501.myfefu.domain.model.ProfileInfo
import ru.jone501.myfefu.domain.model.StudentProfileInfo
import ru.jone501.myfefu.networking.ApiEndpoints.PROFILE_URL
import ru.jone501.myfefu.networking.ApiEndpoints.SCHEDULE_URL
import ru.jone501.myfefu.networking.ApiEndpoints.STUDENT_PROFILE_URL
import ru.jone501.myfefu.networking.api.request.ScheduleRequest
import ru.jone501.myfefu.networking.api.response.BaseResponse

interface ApiService {
    @POST(SCHEDULE_URL)
    suspend fun getSchedule(@Body request: ScheduleRequest): Response<BaseResponse<Lessons>>

    @GET(PROFILE_URL)
    suspend fun getProfile(): Response<BaseResponse<ProfileInfo>>

    @GET(STUDENT_PROFILE_URL)
    suspend fun getStudentProfile(): Response<BaseResponse<List<StudentProfileInfo>>>
}