package dk.scheduling.schedulingfrontend.components

import java.time.format.DateTimeFormatter
import java.util.Locale

val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM dd", Locale.ENGLISH)
val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
val DATE_AND_TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM dd hh:mm a", Locale.ENGLISH)
