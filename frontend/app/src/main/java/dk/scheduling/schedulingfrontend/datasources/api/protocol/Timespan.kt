package dk.scheduling.schedulingfrontend.datasources.api.protocol

import java.time.LocalDateTime

data class Timespan(
    val start: LocalDateTime,
    val end: LocalDateTime,
)
