package dk.scheduling.schedulingfrontend.model

import dk.scheduling.schedulingfrontend.datasources.api.protocol.Device

data class DeviceOverview(
    var device: Device,
    var taskEvent: TaskEvent?,
)
