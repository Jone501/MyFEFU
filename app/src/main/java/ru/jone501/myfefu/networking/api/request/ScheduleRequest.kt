package ru.jone501.myfefu.networking.api.request

class ScheduleRequest(
    val query: String,
    val variables: Map<String, Any> = mapOf(),
    val operationName: String? = null
)