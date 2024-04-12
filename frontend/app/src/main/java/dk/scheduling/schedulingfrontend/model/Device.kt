package dk.scheduling.schedulingfrontend.model

import java.time.LocalDateTime

data class Device(
    var id: Long,
    var name: String,
    var effect: Double?,
)

data class DeviceOverview(
    var device: Device,
    var event: Event?,
)

data class Event(
    var startTime: LocalDateTime,
    var duration: Long,
)
