package dk.scheduling.schedulingfrontend

import dk.scheduling.schedulingfrontend.model.Duration
import org.junit.Test

class DurationTest {
    private val durationValid = Duration("1")
    private val durationUninitialized = Duration("")
    private val durationZero = Duration("0")
    private val durationWhitespace = Duration(" ")
    private val durationContainsWhitespace = Duration("1 ")
    private val durationContainsNonNumeric = Duration("1.1")

    @Test
    fun initializedAndValid() {
        assert(!durationValid.initializedAndInvalid()) { "Valid, 1" }

        assert(!durationUninitialized.initializedAndInvalid()) { "Valid, uninitialized" }

        assert(durationZero.initializedAndInvalid()) { "Invalid, duration cannot be 0" }

        assert(durationWhitespace.initializedAndInvalid()) { "Invalid, duration cannot be whitespace" }

        assert(durationContainsWhitespace.initializedAndInvalid()) { "Invalid, duration cannot contain whitespace" }

        assert(durationContainsNonNumeric.initializedAndInvalid()) { "Invalid, duration can only contain numbers" }
    }

    @Test
    fun statusIsValid() {
        assert(durationValid.status().isValid) { "Valid, 1" }

        assert(!durationUninitialized.status().isValid) { "Invalid, uninitialized" }

        assert(!durationZero.status().isValid) { "Invalid, duration cannot be 0" }

        assert(!durationWhitespace.status().isValid) { "Invalid, duration cannot be whitespace" }

        assert(!durationContainsWhitespace.status().isValid) { "Invalid, duration cannot contain whitespace" }

        assert(!durationContainsNonNumeric.status().isValid) { "Invalid, duration can only contain numbers" }
    }
}
