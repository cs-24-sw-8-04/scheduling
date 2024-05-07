package dk.scheduling.schedulingfrontend.model

import dk.scheduling.schedulingfrontend.datasources.api.protocol.Event
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

sealed class DeviceState {
    data class Active(val event: Event, val duration: Long) : DeviceState()

    data class Scheduled(val event: Event) : DeviceState()

    data object Inactive : DeviceState()
}

fun getDeviceState(deviceOverview: DeviceOverview): DeviceState {
    val dateTimeNow = LocalDateTime.now()

    val taskEvent = deviceOverview.taskEvent ?: return DeviceState.Inactive
    val event = taskEvent.event ?: return DeviceState.Inactive

    if (event.start_time.isAfter(dateTimeNow)) {
        return DeviceState.Scheduled(event)
    }

    if (event.start_time.isBefore(dateTimeNow) && dateTimeNow.isBefore(event.start_time.plus(taskEvent.duration, ChronoUnit.MILLIS))) {
        return DeviceState.Active(event, taskEvent.duration)
    }

    return DeviceState.Inactive
}
