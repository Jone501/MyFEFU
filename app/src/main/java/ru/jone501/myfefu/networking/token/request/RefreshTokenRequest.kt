package ru.jone501.myfefu.networking.token.request

class RefreshTokenRequest(
    val refresh_token: String,
    val grant_type: String = "refresh_token"
)