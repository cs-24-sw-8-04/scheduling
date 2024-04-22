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

    val taskEventComparator =
        Comparator<TaskEvent> { e1, e2 ->
            val hasE1Event = e1.hasEvent()
            val hasE2Event = e2.hasEvent()

            if (!hasE1Event || !hasE2Event) {
                hasE2Event.compareTo(hasE1Event)
            } else {
                e2.event?.start_time?.compareTo(e1.event?.start_time) ?: 0
            }
        }

    val groupTaskEventsToDevice =
        taskEvents.groupBy { it.task.device_id }.mapValues { (_, values) ->
            values.sortedWith(taskEventComparator)
        }

    val deviceTasks: MutableList<DeviceTask> = mutableListOf()

    devicesTestData().sortedBy { it.name }.mapTo(deviceTasks) {
        val deviceTaskEvents = groupTaskEventsToDevice[it.id] ?: mutableListOf()
        DeviceTask(it, deviceTaskEvents.toMutableList())
    }

    return deviceTasks
}
