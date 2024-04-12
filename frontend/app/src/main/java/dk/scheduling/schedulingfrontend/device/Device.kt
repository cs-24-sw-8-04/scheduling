package dk.scheduling.schedulingfrontend.device

import java.time.LocalDateTime

data class Device(
    var id: Int,
    var name: String,
    var effect: Int?,
)

data class DeviceOverview(
    var device: Device,
    var event: Event?,
)

data class Event(
    var startTime: LocalDateTime,
    var duration: Long,
)
