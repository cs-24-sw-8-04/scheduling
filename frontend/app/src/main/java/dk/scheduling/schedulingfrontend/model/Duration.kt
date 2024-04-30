package dk.scheduling.schedulingfrontend.model

data class Duration(val value: String) {
    fun isInitializedAndInvalid(): Boolean = isInitialized() && !status().isValid

    fun status(): Status {
        if (value.toLongOrNull() == null || value.toLongOrNull()!! <= 0) {
            return Status(false, "Duration cannot be blank, 0 or negative")
        }
        return Status(true, value)
    }

    private fun isInitialized(): Boolean = value.isNotBlank()
}
