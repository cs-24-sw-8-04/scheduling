package dk.scheduling.schedulingfrontend.repositories.overviews

import dk.scheduling.schedulingfrontend.model.DeviceOverview
import dk.scheduling.schedulingfrontend.model.DeviceTask
import dk.scheduling.schedulingfrontend.model.TaskEvent
import dk.scheduling.schedulingfrontend.repositories.device.IDeviceRepository
import dk.scheduling.schedulingfrontend.repositories.event.IEventRepository
import dk.scheduling.schedulingfrontend.repositories.task.ITaskRepository

class OverviewRepository(
    private val deviceRepository: IDeviceRepository,
    private val taskRepository: ITaskRepository,
    private val eventRepository: IEventRepository,
) : IOverviewsRepository {
    override suspend fun getDeviceOverview(): List<DeviceOverview> {
        val deviceOverviews: MutableList<DeviceOverview> = mutableListOf()

        getDeviceTasks().mapTo(deviceOverviews) {
            val taskEvent = it.tasks.firstOrNull()?.takeIf { taskEvent -> taskEvent.event != null }
            DeviceOverview(it.device, taskEvent)
        }

        return deviceOverviews
    }

    override suspend fun getDeviceTasks(): List<DeviceTask> {
        val dictionaryEvents = eventRepository.getAllEvents().associateBy { it.task_id }

        val taskEvents: MutableList<TaskEvent> = mutableListOf()
        taskRepository.getAllTasks().mapTo(taskEvents) {
            val event = dictionaryEvents[it.id]
            TaskEvent(it, event)
        }

        val taskEventComparator =
            Comparator<TaskEvent> { e1, e2 ->
                val hasE1Event = e1.hasEvent()
                val hasE2Event = e2.hasEvent()

                if (hasE1Event && hasE2Event) {
                    e2.event?.start_time?.compareTo(e1.event?.start_time) ?: 0
                } else {
                    hasE2Event.compareTo(hasE1Event)
                }
            }

        val groupTaskEventsToDevice =
            taskEvents.groupBy { it.task.device_id }.mapValues { (_, values) ->
                values.sortedWith(taskEventComparator)
            }

        val deviceTasks: MutableList<DeviceTask> = mutableListOf()

        deviceRepository.getAllDevices().sortedBy { it.name }.mapTo(deviceTasks) {
            val deviceTaskEvents = groupTaskEventsToDevice[it.id] ?: mutableListOf()
            DeviceTask(it, deviceTaskEvents.toMutableList())
        }

        return deviceTasks
    }
}
