@file:Suppress("PropertyName")

package dk.scheduling.schedulingfrontend.datasources.api.protocol

import java.time.LocalDateTime

data class Event(
    val id: Long,
    val task_id: Long,
    val start_time: LocalDateTime,
)

data class GetEventsResponse(
    val events: List<Event>,
)
