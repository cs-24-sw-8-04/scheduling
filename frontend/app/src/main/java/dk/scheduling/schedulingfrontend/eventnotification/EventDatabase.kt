package dk.scheduling.schedulingfrontend.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.time.LocalDateTime

@Database(entities = [EventAlarm::class], version = 1)
@TypeConverters(Converters::class)
abstract class EventDatabase : RoomDatabase() {
    abstract fun eventAlarmDao(): EventAlarmDao
}

class Converters {
    @TypeConverter
    fun fromTimestamp(value: String): LocalDateTime {
        return LocalDateTime.parse(value)
    }

    @TypeConverter
    fun dateToTimestamp(dateTime: LocalDateTime): String {
        return dateTime.toString()
    }
}
