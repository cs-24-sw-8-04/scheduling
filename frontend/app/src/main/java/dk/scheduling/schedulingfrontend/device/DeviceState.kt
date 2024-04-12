package dk.scheduling.schedulingfrontend.device

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

enum class DeviceState {
    Active,
    Scheduled,
    Inactive,
}

fun getDeviceState(deviceOverview: DeviceOverview): DeviceState {
    val dateTimeNow = LocalDateTime.now()
    val event = deviceOverview.event ?: return DeviceState.Inactive

    if (event.startTime.isAfter(dateTimeNow)) {
        return DeviceState.Scheduled
    }

    if (event.startTime.isBefore(dateTimeNow) && dateTimeNow.isBefore(event.startTime.plus(event.duration, ChronoUnit.MILLIS))) {
        return DeviceState.Active
    }

    return DeviceState.Inactive
}
