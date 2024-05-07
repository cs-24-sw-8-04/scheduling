package dk.scheduling.schedulingfrontend.eventNotify

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import dk.scheduling.schedulingfrontend.database.EventAlarm
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId

class EventNotifyScheduler(
    private val context: Context,
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    private val notifyBefore = Duration.ofMinutes(30)

    fun scheduler(eventAlarm: EventAlarm) {
        Log.i("EventNotifyScheduler", "scheduler: new alarm to schedule ${eventAlarm.startTime.minus(notifyBefore)}")
        val status =
            if (LocalDateTime.now() < eventAlarm.startTime.minus(notifyBefore)) {
                eventAlarm.startTime.minus(notifyBefore)
            } else {
                LocalDateTime.now()
            }
        Log.i("EventNotifyScheduler", "scheduler: event ${eventAlarm.id} notify at $status. Event start at ${eventAlarm.startTime}")

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
            status.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000,
            pendingIntent,
        )
        Log.i("EventNotifyScheduler", "Set alarm for event ${eventAlarm.id}")
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
