package ru.jone501.myfefu.networking.token.response

data class TokenPairResponse(
    val access_token: String,
    val refresh_token: String
)