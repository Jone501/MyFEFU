package ru.jone501.myfefu.networking.token.request

class CreateTokenRequest(
    val username: String,
    val password: String,
    val scope: List<String> = listOf(
        "profile.info",
        "profile.student",
        "profile.employee",
        "pass.hash"
    ),
    val grant_type: String = "password"
)