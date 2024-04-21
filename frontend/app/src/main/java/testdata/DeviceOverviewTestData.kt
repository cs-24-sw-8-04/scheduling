package testdata

import dk.scheduling.schedulingfrontend.model.DeviceOverview
import java.time.LocalDateTime

fun testDeviceOverview(dateTime: LocalDateTime = LocalDateTime.now()): List<DeviceOverview> {
    val deviceTasks = deviceTaskTestData(dateTime)

    val dest: MutableList<DeviceOverview> = mutableListOf()
    deviceTasks.mapTo(dest) {
        val taskEvent = if (it.tasks.isNotEmpty()) {
            if (it.tasks[0].event != null) {
                it.tasks[0]
            }
            else null
        } else null
        DeviceOverview(it.device, taskEvent)
    }

    return dest
}
