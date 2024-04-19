package dk.scheduling.schedulingfrontend.model

import dk.scheduling.schedulingfrontend.api.protocol.Device
import dk.scheduling.schedulingfrontend.api.protocol.Event
import java.time.LocalDateTime


data class DeviceOverview(
    var device: Device,
    var event: Event?,
)