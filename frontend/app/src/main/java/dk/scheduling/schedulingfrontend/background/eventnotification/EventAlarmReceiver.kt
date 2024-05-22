package dk.scheduling.schedulingfrontend.background.eventnotification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getString
import dk.scheduling.schedulingfrontend.App
import dk.scheduling.schedulingfrontend.R
import dk.scheduling.schedulingfrontend.datasources.eventalarm.EventAlarm
import dk.scheduling.schedulingfrontend.gui.components.DATE_AND_TIME_FORMAT
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.random.Random

class EventAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context?,
        intent: Intent?,
    ) {
        context ?: return

        val data = intent?.extras ?: return

        val id = if (data.containsKey("ID")) data.getLong("ID") else return
        Thread {
            val dbo = App.eventAlarmDb.eventAlarmDao()
            val eventAlarm = dbo.loadById(id) ?: return@Thread

            val notification = createEventNotification(eventAlarm)

            showNotification(Random.nextInt(), context, notification)

            if (!notification.startEvent) {
                EventNotifyScheduler(context).scheduler(eventAlarm)
            }
        }.start()
    }

    private fun createEventNotification(eventAlarm: EventAlarm): EventNotification {
        return if (LocalDateTime.now() <= eventAlarm.startTime) {
            val minutesBack = ChronoUnit.MINUTES.between(LocalDateTime.now(), eventAlarm.startTime)
            EventNotification(
                textTitle = "$minutesBack minutes to start ${eventAlarm.deviceName}",
                textContent = "Please start ${eventAlarm.deviceName} at ${eventAlarm.startTime.format(DATE_AND_TIME_FORMAT)}",
                startEvent = false,
            )
        } else {
            val endDateTime = eventAlarm.startTime.plus(Duration.ofMillis(eventAlarm.duration))
            EventNotification(
                textTitle = "Start ${eventAlarm.deviceName} now",
                textContent = "This event ends at ${endDateTime.format(DATE_AND_TIME_FORMAT)}",
                startEvent = true,
            )
        }
    }
}

fun showNotification(
    notificationId: Int,
    context: Context,
    notification: EventNotification,
) {
    val builder =
        NotificationCompat.Builder(context, getString(context, R.string.notification_channel))
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(notification.textTitle)
            .setContentText(notification.textContent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    App.notificationManager.notify(notificationId, builder.build())
}

data class EventNotification(
    val textTitle: String,
    val textContent: String,
    val startEvent: Boolean,
)
