package dk.scheduling.schedulingfrontend.eventNotify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class EventAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context?,
        intent: Intent?,
    ) {
        val message = intent?.getStringExtra("MESSAGE") ?: return
        println("EVENT: " + message)
    }
}
