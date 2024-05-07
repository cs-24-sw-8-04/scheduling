package dk.scheduling.schedulingfrontend

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.room.Room
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import dk.scheduling.schedulingfrontend.background.eventCollectWork
import dk.scheduling.schedulingfrontend.background.eventCollectWorkOnetime
import dk.scheduling.schedulingfrontend.database.EventDatabase
import dk.scheduling.schedulingfrontend.module.AppModule
import dk.scheduling.schedulingfrontend.module.IAppModule
import java.time.Duration

class App : Application() {
    companion object {
        lateinit var appModule: IAppModule
        lateinit var eventAlarmDb: EventDatabase
        lateinit var notificationManager: NotificationManager
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        appModule = AppModule(this)

        eventAlarmDb =
            Room.databaseBuilder(
                context = this,
                klass = EventDatabase::class.java,
                name = ContextCompat.getString(this, R.string.event_alarm_local_database),
            ).build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    getString(R.string.notification_channel),
                    "Notification Channel",
                    NotificationManager.IMPORTANCE_HIGH,
                )

            notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val workManager = WorkManager.getInstance(this)

        workManager.enqueueUniquePeriodicWork(
            getString(R.string.event_alarm_notify_work_id),
            ExistingPeriodicWorkPolicy.KEEP,
            eventCollectWork(Duration.ofMinutes(15)),
        )

        workManager.enqueueUniqueWork(
            "Start-up",
            ExistingWorkPolicy.KEEP,
            eventCollectWorkOnetime(),
        )
    }
}
