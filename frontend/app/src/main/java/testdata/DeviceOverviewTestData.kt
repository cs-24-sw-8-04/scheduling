package testdata

import dk.scheduling.schedulingfrontend.api.protocol.Device
import dk.scheduling.schedulingfrontend.api.protocol.Event
import dk.scheduling.schedulingfrontend.model.DeviceOverview
import dk.scheduling.schedulingfrontend.model.DeviceTask
import dk.scheduling.schedulingfrontend.model.EventTask
import java.time.LocalDateTime

fun testDeviceOverview(dateTime: LocalDateTime = LocalDateTime.now()): List<DeviceOverview> {
    val deviceTasks = deviceTaskTestData(dateTime)

    val dest: MutableList<DeviceOverview> = mutableListOf()
    deviceTasks.mapTo(dest) {
        val event = if (it.tasks.isNotEmpty()) it.tasks[0].event else null
        DeviceOverview(it.device, event)
    }

    return dest
}
