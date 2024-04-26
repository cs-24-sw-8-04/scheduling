package testdata

import dk.scheduling.schedulingfrontend.api.protocol.Task
import dk.scheduling.schedulingfrontend.api.protocol.Timespan
import dk.scheduling.schedulingfrontend.repositories.task.ITaskRepository
import kotlinx.coroutines.delay

class DummyTaskRepository(private val sleepDuration: Long = 2000) : ITaskRepository {
    private val tasks = tasksTestData().toMutableList()

    override suspend fun getAllTasks(): List<Task> {
        delay(sleepDuration)
        return tasks.toList()
    }

    override suspend fun createTask(
        timeSpan: Timespan,
        duration: Long,
        device_id: Long,
    ) {
        delay(sleepDuration)
        val newTaskId = tasks.maxOf { task -> task.device_id } + 1
        tasks.add(Task(newTaskId, timeSpan, duration, device_id))
    }

    override suspend fun deleteTask(taskId: Long) {
        delay(sleepDuration)
        tasks.removeAll { task -> task.id == taskId }
    }
}
