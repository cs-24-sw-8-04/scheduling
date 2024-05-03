package dk.scheduling.schedulingfrontend.components

import java.time.format.DateTimeFormatter
import java.util.Locale

val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM dd", Locale.ENGLISH)
val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
val DATE_AND_TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM dd HH:mm")
