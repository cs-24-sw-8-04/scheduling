package dk.scheduling.schedulingfrontend

import dk.scheduling.schedulingfrontend.gui.components.DateRange
import org.junit.Test

class DateRangeTest {
    @Test
    fun dateRangeTest() {
        val dateValid = DateRange(0, Long.MAX_VALUE)

        assert(dateValid.isInitialized()) { "Range values set" }
        assert(dateValid.status().isValid) { "Valid range" }

        val dateUninitialized = DateRange(null, null)
        val dateMissingStart = DateRange(null, 0)
        val dateMissingEnd = DateRange(Long.MAX_VALUE, null)

        assert(!dateUninitialized.status().isValid) { "Invalid range, not initialized, both fields are null" }
        assert(!dateMissingStart.status().isValid) { "Invalid range, start date not initialized" }
        assert(!dateMissingEnd.status().isValid) { "Invalid range, end date not initialized" }

        val dateEndBeforeStart = DateRange(Long.MAX_VALUE, 0)
        assert(!dateEndBeforeStart.status().isValid) { "Invalid range, end date is before start date" }
    }
}
