package dk.scheduling.schedulingfrontend.model

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import dk.scheduling.schedulingfrontend.components.DateRange
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
data class TaskForm(
    val deviceId: Long?,
    val duration: Duration,
    val dateRange: DateRange,
    val startTime: TimePickerState,
    val endTime: TimePickerState,
) {
    fun status(): Status {
        return if (deviceId == null) {
            return Status(false, "No device selected")
        } else if (!duration.status().isValid) {
            duration.status()
        } else if (!dateRange.status().isValid) {
            dateRange.status()
        } else if (!timeStatus().isValid) {
            timeStatus()
        } else {
            // Indicate the task is now valid
            Status(true, "")
        }
    }

    fun startDateTime(): LocalDateTime {
        return dateRange.rangeStart()!!.plusHours(startTime.hour.toLong()).plusMinutes(startTime.minute.toLong())
    }

    fun endDateTime(): LocalDateTime {
        return dateRange.rangeEnd()!!.plusHours(endTime.hour.toLong()).plusMinutes(endTime.minute.toLong())
    }

    private fun timeStatus(): Status {
        val start = startDateTime()
        val end = endDateTime()
        val intervalLengthInMinutes = start.until(end, ChronoUnit.MINUTES)
        val latestPossibleEventStartTime = end.minus(intervalLengthInMinutes, ChronoUnit.MINUTES)
        return if (!start.isBefore(end)) {
            Status(false, "Start time must be before end time, change times or date interval")
        } else if (intervalLengthInMinutes < duration.value.toLong()) {
            Status(false, "The duration may not be larger than the specified interval")
        } else if (!latestPossibleEventStartTime.isAfter(LocalDateTime.now())) {
            Status(false, "There is not enough time perform the task before the deadline")
        } else {
            Status(true, "")
        }
    }

    fun printStartTime(): String {
        return formatTime(startTime.hour, startTime.minute)
    }

    fun printEndTime(): String {
        return formatTime(endTime.hour, endTime.minute)
    }

    private fun formatTime(
        hour: Int,
        minute: Int,
    ): String {
        val hourStr = if (hour < 10) "0$hour" else hour
        val minuteStr = if (minute < 10) "0$minute" else minute
        return "$hourStr:$minuteStr"
    }
}
