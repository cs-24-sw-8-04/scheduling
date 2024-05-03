package dk.scheduling.schedulingfrontend.database

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.time.LocalDateTime

@Entity
data class EventAlarm(
    @PrimaryKey val id: Long = 0,
    val deviceName: String,
    val startTime: LocalDateTime,
    val duration: Long,
)

@Dao
interface EventAlarmDao {
    @Query("SELECT * FROM eventAlarm")
    fun getAll(): List<EventAlarm>

    @Query("SELECT * FROM eventAlarm WHERE id == (:id)")
    fun loadById(id: Long): EventAlarm

    @Insert
    fun insert(eventAlarm: EventAlarm): Long

    @Delete
    fun delete(eventAlarm: EventAlarm)
}

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
