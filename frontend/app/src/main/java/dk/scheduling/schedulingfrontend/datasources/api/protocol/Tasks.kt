@file:Suppress("PropertyName")

package dk.scheduling.schedulingfrontend.datasources.api.protocol

data class Task(
    val id: Long,
    val timespan: Timespan,
    // Milliseconds
    val duration: Long,
    val device_id: Long,
)

data class GetTasksResponse(
    val tasks: List<Task>,
)

data class CreateTaskRequest(
    val timespan: Timespan,
    // Milliseconds
    val duration: Long,
    val device_id: Long,
)
