package dk.scheduling.schedulingfrontend.device

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

enum class DeviceState {
    Active,
    Scheduled,
    Inactive,
}

fun getDeviceState(deviceOverview: DeviceOverview): DeviceState {
    val event = deviceOverview.event ?: return DeviceState.Inactive

    if (event.startTime.isAfter(LocalDateTime.now())) {
        return DeviceState.Scheduled
    }

    if (event.startTime.isBefore(
            LocalDateTime.now(),
        ) && LocalDateTime.now().isBefore(event.startTime.plus(event.duration, ChronoUnit.MILLIS))
    ) {
        return DeviceState.Active
    }

    return DeviceState.Inactive
}
