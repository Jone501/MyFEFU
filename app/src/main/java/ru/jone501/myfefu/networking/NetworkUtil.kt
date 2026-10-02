package ru.jone501.myfefu.networking

object ApiEndpoints {
    const val AUTH_HOST = "https://esa.dvfu.ru"
    const val CREATE_TOKEN_URL = "/oauth/token/create"
    const val REFRESH_TOKEN_URL = "/oauth/token/refresh"

    const val API_HOST = "https://blackbox.dvfu.ru"
    const val SCHEDULE_URL = "/v2/graphql/schedule"
    const val PROFILE_URL = "/v2/profile/get"
    const val STUDENT_PROFILE_URL = "/v2/student-profile/get"

    const val USER_AGENT: String = "User-Agent: MyFEFU/1.0.0"
}