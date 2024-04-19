@file:Suppress("PropertyName")

package dk.scheduling.schedulingfrontend.api.protocol

data class Task(
    val id: Long,
    val timespan: Timespan,
    val duration: Long, // Milliseconds
    val device_id: Long,
)

data class GetTasksResponse(
    val tasks: List<Task>,
)

data class CreateTaskRequest(
    val timespan: Timespan,
    val duration: Long, // Milliseconds
    val device_id: Long,
)
