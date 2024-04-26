package dk.scheduling.schedulingfrontend.repositories.task

import dk.scheduling.schedulingfrontend.api.protocol.Task
import dk.scheduling.schedulingfrontend.api.protocol.Timespan

interface ITaskRepository {
    suspend fun getAllTasks(): List<Task>

    suspend fun createTask(
        timeSpan: Timespan,
        duration: Long,
        device_id: Long,
    )

    suspend fun deleteTask(taskId: Long)
}
