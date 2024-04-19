package dk.scheduling.schedulingfrontend.model

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

    if (event.start_time.isAfter(dateTimeNow)) {
        return DeviceState.Scheduled
    }

    if (event.start_time.isBefore(dateTimeNow) && dateTimeNow.isBefore(event.start_time.plus(10, ChronoUnit.MILLIS))) {
        return DeviceState.Active
    }

    return DeviceState.Inactive
}
