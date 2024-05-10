package dk.scheduling.schedulingfrontend.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity
data class EventAlarm(
    @PrimaryKey val id: Long = 0,
    val deviceName: String,
    val startTime: LocalDateTime,
    val duration: Long,
)
