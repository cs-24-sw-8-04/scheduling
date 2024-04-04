package dk.scheduling.schedulingfrontend.api.protocol

data class Task(
    val id: Long,
    val timespan: Timespan,
    val duration: Long,
    val device_id: Long,
)

data class CreateTaskRequest(
    val timespan: Timespan,
    val duration: Long,
    val device_id: Long,
)

data class DeleteTaskRequest(
    val id: Long,
)
