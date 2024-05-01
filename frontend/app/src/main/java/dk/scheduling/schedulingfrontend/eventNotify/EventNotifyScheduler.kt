package dk.scheduling.schedulingfrontend.eventNotify

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dk.scheduling.schedulingfrontend.database.EventAlarm
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId

class EventNotifyScheduler(
    private val context: Context,
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    companion object {
        val notifyBefore = Duration.ofMinutes(10)
    }

    fun scheduler(eventAlarm: EventAlarm) {
        val status =
            if (LocalDateTime.now().isBefore(eventAlarm.startTime.minus(notifyBefore))) {
                eventAlarm.startTime.minusMinutes(10)
            } else {
                eventAlarm.startTime
            }

        val intent =
            Intent(context, EventAlarmReceiver::class.java).apply {
                putExtra("ID", eventAlarm.id)
            }

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                eventAlarm.hashCode(),
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            eventAlarm.startTime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000,
            pendingIntent,
        )
    }

    fun cancel(eventAlarm: EventAlarm) {
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context,
                eventAlarm.hashCode(),
                Intent(context, EventAlarmReceiver::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            ),
        )
    }
}
