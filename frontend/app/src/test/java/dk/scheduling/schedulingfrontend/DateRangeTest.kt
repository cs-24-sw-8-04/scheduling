package dk.scheduling.schedulingfrontend

import dk.scheduling.schedulingfrontend.components.DateRange
import org.junit.Test

class DateRangeTest {
    @Test
    fun dateRangeTest() {
        val date1 = DateRange(0, Long.MAX_VALUE)
        assert(date1.isValidRange()) { "Valid range" }

        assert(date1.getStartDate() == "1970-01-01") { "Should equal epoch date, not ${date1.getStartDate()}" }
        assert(date1.getEndDate() == "+292278994-08-17") { "Should equal 17-08-292278994, not ${date1.getEndDate()}" }

        val date2 = DateRange(Long.MAX_VALUE, Long.MIN_VALUE)
        assert(!date2.isValidRange()) { "Invalid range, startTime > endTime" }

        val date3 = DateRange()
        assert(!date3.isValidRange()) { "Invalid range, not initialized, both fields are null" }
    }
}
