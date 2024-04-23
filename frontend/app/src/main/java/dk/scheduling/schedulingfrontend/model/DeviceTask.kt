package dk.scheduling.schedulingfrontend.model

import dk.scheduling.schedulingfrontend.api.protocol.Device

data class DeviceTask(
    val device: Device,
    val tasks: MutableList<TaskEvent>,
)
