package dk.scheduling.schedulingfrontend.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface EventAlarmDao {
    @Query("SELECT * FROM eventAlarm")
    fun getAll(): List<EventAlarm>

    @Query("SELECT * FROM eventAlarm WHERE id == (:id)")
    fun loadById(id: Long): EventAlarm?

    @Insert
    fun insert(eventAlarm: EventAlarm): Long

    @Delete
    fun delete(eventAlarm: EventAlarm)
}
