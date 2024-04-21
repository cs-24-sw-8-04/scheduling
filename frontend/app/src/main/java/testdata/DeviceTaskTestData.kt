package testdata

import dk.scheduling.schedulingfrontend.model.DeviceTask
import dk.scheduling.schedulingfrontend.model.TaskEvent
import java.time.LocalDateTime

fun deviceTaskTestData(dateTime: LocalDateTime = LocalDateTime.now()): List<DeviceTask> {
    val dictionaryEvents = eventsTestData(dateTime).associateBy { it.task_id }

    val taskEvents: MutableList<TaskEvent> = mutableListOf()
    tasksTestData(dateTime).mapTo(taskEvents) {
        val event = dictionaryEvents[it.id]
        TaskEvent(it, event)
    }

    val groupTaskEventsToDevice = taskEvents.groupBy { it.task.device_id }

    val deviceTasks: MutableList<DeviceTask> = mutableListOf()

    devicesTestData().mapTo(deviceTasks) {
        val deviceTaskEvents = groupTaskEventsToDevice[it.id] ?: mutableListOf()
        DeviceTask(it, deviceTaskEvents.toMutableList())
    }

    return deviceTasks
}
