package ru.jone501.myfefu.networking.api.response

class BaseResponse<T>(
    val success: Boolean,
    val data: T
)