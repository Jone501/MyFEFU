package ru.jone501.myfefu.networking.api.response

import ru.jone501.myfefu.domain.model.Lessons

data class ScheduleResponse(
    var data: Lessons
)