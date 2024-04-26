package testdata

import dk.scheduling.schedulingfrontend.api.protocol.Task
import dk.scheduling.schedulingfrontend.api.protocol.Timespan
import dk.scheduling.schedulingfrontend.repositories.task.ITaskRepository

class DummyTaskRepository : ITaskRepository {
    private val tasks = tasksTestData().toMutableList()

    override suspend fun getAllTasks(): List<Task> {
        return tasks.toList()
    }

    override suspend fun createTask(
        timeSpan: Timespan,
        duration: Long,
        device_id: Long,
    ) {
        val newTaskId = tasks.maxOf { task -> task.device_id } + 1
        tasks.add(Task(newTaskId, timeSpan, duration, device_id))
    }

    override suspend fun deleteTask(taskId: Long) {
        tasks.removeAll { task -> task.id == taskId }
    }
}
