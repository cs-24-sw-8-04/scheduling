package dk.scheduling.schedulingfrontend

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import dk.scheduling.schedulingfrontend.components.DateRange
import dk.scheduling.schedulingfrontend.pages.isValidDuration
import dk.scheduling.schedulingfrontend.pages.isValidInput
import org.junit.Test

@OptIn(ExperimentalMaterial3Api::class)
class CreateTaskPageTest {
    private val validTime = TimePickerState(0, 0, true)
    private val validTimeMidday = TimePickerState(12, 0, true)
    private val validTimeOneMinutePastMidday = TimePickerState(12, 1, true)

    @Test
    fun isValidDurationTest() {
        assert(isValidDuration("1")) { "Valid" }
        assert(!isValidDuration("0")) { "Invalid, cannot be 0" }
        assert(!isValidDuration("00000")) { "Invalid, cannot be 0" }
        assert(!isValidDuration(" 1")) { "Invalid, cannot contain whitespace" }
        assert(!isValidDuration("1.1")) { "Invalid, cannot contain dot" }
        assert(!isValidDuration("1-1")) { "Invalid, cannot contain dash" }
        assert(!isValidDuration("1,1")) { "Invalid, cannot contain comma" }
    }

    @Test
    fun isValidInputTest() {
        val result1 = isValidInput("1", DateRange(Long.MIN_VALUE, Long.MAX_VALUE), validTime, validTime)
        assert(result1) { "Valid" }

        val result3 = isValidInput("1", DateRange(Long.MIN_VALUE, Long.MAX_VALUE), validTimeMidday, validTime)
        assert(result3) { "Valid, End time > start time, but start date < end date" }

        val result4 = isValidInput("1", DateRange(Long.MIN_VALUE, Long.MIN_VALUE), validTimeMidday, validTime)
        assert(!result4) { "Invalid, End time > start time and start date == end date" }

        val result5 = isValidInput("1", DateRange(Long.MIN_VALUE, Long.MIN_VALUE), validTimeMidday, validTimeOneMinutePastMidday)
        assert(result5) { "Valid, start date == end date, and end time hour == start time hour, and start time minute < end time minute" }

        val result6 = isValidInput("1", DateRange(Long.MIN_VALUE, Long.MIN_VALUE), validTimeOneMinutePastMidday, validTimeMidday)
        assert(!result6) { "Valid, start date == end date and end time hour == start time hour, and start time minute < end time minute" }
    }
}
