package testdata

import dk.scheduling.schedulingfrontend.model.DeviceTask
import dk.scheduling.schedulingfrontend.model.EventTask
import java.time.LocalDateTime

fun deviceTaskTestData(dateTime: LocalDateTime = LocalDateTime.now()): List<DeviceTask> {
    val dictionaryEvents = eventsTestData(dateTime).associateBy { it.task_id }

    val eventTasks: MutableList<EventTask> = mutableListOf()
    tasksTestData(dateTime).mapTo(eventTasks) {
        val event = dictionaryEvents[it.id]
        EventTask(it, event)
    }

    val groupTaskEventsToDevice = eventTasks.groupBy { it.task.device_id }

    val deviceTasks: MutableList<DeviceTask> = mutableListOf()

    devicesTestData().mapTo(deviceTasks) {
        val taskEvents = groupTaskEventsToDevice[it.id] ?: mutableListOf()
        DeviceTask(it, taskEvents)
    }

    return deviceTasks
}
