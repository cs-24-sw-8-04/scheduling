package dk.scheduling.schedulingfrontend

import dk.scheduling.schedulingfrontend.model.Duration
import org.junit.Test

class DurationTest {
    private val durationValid = Duration("1")
    private val durationUninitialized = Duration("")
    private val durationWhitespace = Duration(" ")
    private val durationContainsWhitespace = Duration("1 ")
    private val durationZero = Duration("0")
    private val durationNegative = Duration("-1")
    private val durationContainsNonNumeric = Duration("1.1")

    @Test
    fun initializedAndValid() {
        assert(!durationValid.initializedAndInvalid()) { "Valid, 1" }

        assert(!durationUninitialized.initializedAndInvalid()) { "Valid, uninitialized" }

        assert(!durationWhitespace.initializedAndInvalid()) { "Valid, uninitialized" }

        assert(durationContainsWhitespace.initializedAndInvalid()) { "Invalid, no whitespace" }

        assert(durationZero.initializedAndInvalid()) { "Invalid, duration cannot be 0" }

        assert(durationNegative.initializedAndInvalid()) { "Invalid, duration cannot be negative" }

        assert(durationContainsNonNumeric.initializedAndInvalid()) { "Invalid, duration can only contain numbers" }
    }

    @Test
    fun statusIsValid() {
        assert(durationValid.status().isValid) { "Valid, 1" }

        assert(!durationUninitialized.status().isValid) { "Invalid, uninitialized" }

        assert(!durationWhitespace.status().isValid) { "Invalid, duration cannot be whitespace" }

        assert(!durationContainsWhitespace.status().isValid) { "Invalid, duration cannot contain whitespace" }

        assert(!durationZero.status().isValid) { "Invalid, duration cannot be 0" }

        assert(!durationNegative.status().isValid) { "Invalid, duration cannot be negative" }

        assert(!durationContainsNonNumeric.status().isValid) { "Invalid, duration can only contain numbers" }
    }
}
