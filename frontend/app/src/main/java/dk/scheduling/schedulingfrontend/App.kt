package dk.scheduling.schedulingfrontend

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.room.Room
import dk.scheduling.schedulingfrontend.database.EventDatabase
import dk.scheduling.schedulingfrontend.module.AppModule
import dk.scheduling.schedulingfrontend.module.IAppModule

class App : Application() {
    companion object {
        lateinit var appModule: IAppModule
        lateinit var eventAlarmDb: EventDatabase
    }

    override fun onCreate() {
        super.onCreate()
        Log.i("INFO", "onCreate: AppModule")
        appModule = AppModule(this)
        Log.i("INFO", "onCreate: eventAlarmDb")
        eventAlarmDb =
            Room.databaseBuilder(
                context = applicationContext,
                klass = EventDatabase::class.java,
                name = ContextCompat.getString(this, R.string.event_alarm_local_database),
            ).build()
        Log.i("LOGGER", "add channel to notificationManager")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    getString(R.string.notification_channel),
                    "Notification Channel",
                    NotificationManager.IMPORTANCE_HIGH, // TODO: CHECK
                )

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
