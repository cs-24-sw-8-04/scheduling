package dk.scheduling.schedulingfrontend.api.protocol

data class Event(
    val id: Long,
    val task_id: Long,
    // TODO: Change to Date
    val start_time: String,
)

data class GetEventsResponse(
    val events: List<Event>,
)
