package dk.scheduling.schedulingfrontend

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import dk.scheduling.schedulingfrontend.components.DateRange
import dk.scheduling.schedulingfrontend.model.Duration
import dk.scheduling.schedulingfrontend.model.TaskForm
import org.junit.Test

@OptIn(ExperimentalMaterial3Api::class)
class TaskFormTest {
    private val timeMidday = TimePickerState(12, 0, true)
    private val timeOneMinutePastMidday = TimePickerState(12, 1, true)

    @Test
    fun statusIsValidTest() {
        val taskSameDate =
            TaskForm(
                1,
                Duration("1"),
                DateRange(0, 0),
                timeMidday,
                timeOneMinutePastMidday,
            )
        assert(taskSameDate.status().isValid) { "Valid task, same date" }

        val taskDifferentDates =
            TaskForm(
                1,
                Duration("1"),
                DateRange(0, Long.MAX_VALUE),
                timeMidday,
                timeMidday,
            )
        assert(taskDifferentDates.status().isValid) { "Valid task, different dates" }
    }

    @Test
    fun durationLargerThanInterval_InvalidStatus() {
        val taskSameDate =
            TaskForm(
                1,
                Duration("2"),
                DateRange(0, 0),
                timeMidday,
                timeOneMinutePastMidday,
            )
        assert(!taskSameDate.status().isValid) { "Invalid task, time interval 1 minute is smaller than duration" }

        val taskDifferentDates =
            TaskForm(
                1,
                // 24 hours and one minute in minutes
                Duration("1441"),
                // One day in minutes
                DateRange(0, 86400000),
                timeMidday,
                timeMidday,
            )
        assert(!taskDifferentDates.status().isValid) { "Invalid task, time interval 1 day is smaller than duration" }
    }
}
