package dk.scheduling.schedulingfrontend.model

import dk.scheduling.schedulingfrontend.api.protocol.Device
import dk.scheduling.schedulingfrontend.api.protocol.Event
import dk.scheduling.schedulingfrontend.api.protocol.Task

data class DeviceTask(
    val device: Device,
    val tasks: MutableList<EventTask>,
)

data class EventTask(
    val task: Task,
    val event: Event?,
)
