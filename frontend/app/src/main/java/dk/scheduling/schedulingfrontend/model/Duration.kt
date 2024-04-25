package dk.scheduling.schedulingfrontend.model

data class Duration(val value: String) {
    fun initializedAndInvalid(): Boolean {
        return isInitialized() && !status().isValid
    }

    fun status(): Status {
        if (isNullOrEmptyOrZero(value)) {
            return Status(false, "Duration cannot be blank or 0")
        }
        if (!isNumbersOnly(value)) {
            return Status(false, "Duration must only be numbers")
        }
        return Status(true, value)
    }

    private fun isInitialized(): Boolean {
        return value.isNotEmpty()
    }

    private fun isNumbersOnly(input: String): Boolean {
        return input.all { char -> char.isDigit() }
    }

    private fun isNullOrEmptyOrZero(input: String): Boolean {
        return !isInitialized() || input.isBlank() || input.all { char -> char == '0' }
    }
}
