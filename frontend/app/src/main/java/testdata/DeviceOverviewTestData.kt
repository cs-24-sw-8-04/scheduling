package testdata

import dk.scheduling.schedulingfrontend.model.DeviceOverview
import java.time.LocalDateTime

fun testDeviceOverview(dateTime: LocalDateTime = LocalDateTime.now()): List<DeviceOverview> {
    val deviceTasks = deviceTaskTestData(dateTime)

    val dest: MutableList<DeviceOverview> = mutableListOf()
    deviceTasks.mapTo(dest) {
        val taskEvent = it.tasks.firstOrNull()?.takeIf { taskEvent -> taskEvent.event != null }
        DeviceOverview(it.device, taskEvent)
    }

    return dest
}
