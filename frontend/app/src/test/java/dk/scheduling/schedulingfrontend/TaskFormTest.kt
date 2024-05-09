package dk.scheduling.schedulingfrontend

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import dk.scheduling.schedulingfrontend.components.DateRange
import dk.scheduling.schedulingfrontend.model.Duration
import dk.scheduling.schedulingfrontend.model.TaskForm
import org.junit.Test
import java.time.Instant
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
class TaskFormTest {
    private val timeMidday = TimePickerState(12, 0, true)
    private val timeOneMinutePastMidday = TimePickerState(12, 1, true)
    private val millisecondsInADay = 86400000L
    private val timeSinceEpochInMillis = Instant.now().toEpochMilli()
    private val tomorrowInMillis = timeSinceEpochInMillis + millisecondsInADay

    @Test
    fun statusIsValidTest() {
        val taskSameDate =
            TaskForm(
                1,
                Duration("1"),
                DateRange(tomorrowInMillis, tomorrowInMillis),
                timeMidday,
                timeOneMinutePastMidday,
            )
        assert(taskSameDate.status().isValid) { "Valid task, same date" }

        val taskDifferentDates =
            TaskForm(
                1,
                Duration("1"),
                DateRange(timeSinceEpochInMillis, tomorrowInMillis),
                timeMidday,
                timeMidday,
            )
        assert(taskDifferentDates.status().isValid) { "Valid task, different dates" }

        // Say the current time is 14:10
        // Then the start time is 14:00 and end time is 14:20
        // The duration of the task is 7 minutes, which fits the interval.
        val timeLocal = LocalDateTime.now().minusMinutes(10)
        val timeLocalEnd = timeLocal.plusMinutes(20)
        val taskTimeNowCloseToEndTime =
            TaskForm(
                1,
                Duration("7"),
                DateRange(tomorrowInMillis, tomorrowInMillis),
                TimePickerState(timeLocal.hour, timeLocal.minute, true),
                TimePickerState(timeLocalEnd.hour, timeLocalEnd.minute, true),
            )
        // If the task is scheduled instantly, then the event could start at 14:10.
        // This would leave enough time to perform the task before the end time:
        // 14:10 (current time) + 0:07 (the duration)  <  14:20 (end time)
        assert(taskTimeNowCloseToEndTime.status().isValid) { "Valid task, end time is after (current time + duration)" }
    }

    @Test
    fun durationLargerThanInterval_InvalidStatus() {
        val taskSameDate =
            TaskForm(
                1,
                Duration("2"),
                DateRange(tomorrowInMillis, tomorrowInMillis),
                timeMidday,
                timeOneMinutePastMidday,
            )
        assert(!taskSameDate.status().isValid) { "Invalid task, time interval 1 minute is smaller than duration" }

        val taskDifferentDates =
            TaskForm(
                1,
                // 24 hours and one minute in minutes
                Duration("1441"),
                // One day in milliseconds
                DateRange(timeSinceEpochInMillis, tomorrowInMillis),
                timeMidday,
                timeMidday,
            )
        assert(!taskDifferentDates.status().isValid) { "Invalid task, time interval 1 day is smaller than duration" }
    }

    @Test
    fun deviceId_IsNull_InvalidStatus() {
        val taskSameDate =
            TaskForm(
                null,
                Duration("1"),
                DateRange(tomorrowInMillis, tomorrowInMillis),
                timeMidday,
                timeOneMinutePastMidday,
            )
        assert(!taskSameDate.status().isValid) { "Invalid task, deviceId is null (unselected)" }
    }

    @Test
    fun endTime_before_currentTimePlusDuration_InvalidStatus() {
        // Say the current time is 14:04
        // Then the start time is 14:00 and end time is 14:06
        // The duration of the task is 4 minutes, which fits the interval.
        val timeLocal = LocalDateTime.now().minusMinutes(4)
        val timeLocalEnd = timeLocal.plusMinutes(6)

        val timeNowCloseToEndTime =
            TaskForm(
                1,
                Duration("4"),
                DateRange(timeSinceEpochInMillis, timeSinceEpochInMillis),
                TimePickerState(timeLocal.hour, timeLocal.minute, true),
                TimePickerState(timeLocalEnd.hour, timeLocalEnd.minute, true),
            )
        // The current time is 14:04, and even if the task is instantly scheduled and started/executed,
        // then the event would be 14:04.
        // But this does not leave enough time to run the 4 minute duration before the end time 14:06
        // 14:04 (current time) + 0:04 (the duration)  >  14:06 (end time)  -> Impossible!
        assert(!timeNowCloseToEndTime.status().isValid) { "Invalid task, end time is before (current time + duration)" }
    }
}
