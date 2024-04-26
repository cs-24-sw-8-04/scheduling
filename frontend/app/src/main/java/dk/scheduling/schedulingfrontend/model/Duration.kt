package dk.scheduling.schedulingfrontend.model

data class Duration(val value: String) {
    fun isInitializedAndInvalid(): Boolean {
        return isInitialized() && !status().isValid
    }

    fun status(): Status {
        if (cannotParseToLong(value)) {
            return Status(false, "Duration cannot be blank or 0")
        }
        return Status(true, value)
    }

    private fun isInitialized(): Boolean {
        return value.isNotBlank()
    }

    private fun cannotParseToLong(input: String): Boolean {
        return !isInitialized() || input.toLongOrNull() == null || input.toLongOrNull()!! <= 0
    }
}
