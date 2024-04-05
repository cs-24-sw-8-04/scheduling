package dk.scheduling.schedulingfrontend.api.protocol

data class Event(
    val id: Long,
    val task_id: Long,
    val start_time: String, // TODO: Change to Date
)

data class GetEventsResponse(
    val events: List<Event>,
)
