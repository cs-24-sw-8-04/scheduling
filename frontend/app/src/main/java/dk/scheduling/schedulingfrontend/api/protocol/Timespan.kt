package dk.scheduling.schedulingfrontend.api.protocol

import java.time.LocalDateTime

data class Timespan(
    val start: LocalDateTime,
    val end: LocalDateTime,
)
