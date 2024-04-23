package dk.scheduling.schedulingfrontend.model

import dk.scheduling.schedulingfrontend.api.protocol.Device

data class DeviceOverview(
    var device: Device,
    var taskEvent: TaskEvent?,
)
