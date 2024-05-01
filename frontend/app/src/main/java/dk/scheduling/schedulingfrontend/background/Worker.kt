package dk.scheduling.schedulingfrontend.background

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import dk.scheduling.schedulingfrontend.App
import dk.scheduling.schedulingfrontend.database.EventAlarm
import dk.scheduling.schedulingfrontend.eventNotify.EventNotifyScheduler
import java.time.Duration

fun eventCollectWork(periodic: Duration): PeriodicWorkRequest {
    val constraints =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    return PeriodicWorkRequestBuilder<EventCollectorWorker>(periodic)
        .setConstraints(constraints)
        .build()
}

class EventCollectorWorker(
    private val context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val dao = App.eventAlarmDb.eventAlarmDao()
        val eventNotifyScheduler = EventNotifyScheduler(context)

        val eventsIdStored = dao.getAll().map { it.id }.toSet()
        val retrievedEventIds = mutableSetOf<Long>()
        App.appModule.overviewRepo.getDeviceTasks().forEach { deviceTask ->
            deviceTask.tasks.forEach { taskEvent ->
                if (taskEvent.event != null) {
                    val id = taskEvent.event.id
                    if (!(eventsIdStored.contains(id))) {
                        val eventAlarm =
                            EventAlarm(
                                id = id,
                                deviceName = deviceTask.device.name,
                                startTime = taskEvent.event.start_time,
                            )
                        dao.insert(eventAlarm)
                        eventNotifyScheduler.scheduler(eventAlarm)
                        retrievedEventIds.add(id)
                    }
                }
            }
        }

        val cancelEvents = eventsIdStored - retrievedEventIds
        cancelEvents.forEach {
            eventNotifyScheduler.scheduler(dao.loadById(it))
        }

        return Result.success()
    }
}

class EventAlarmSchedulerWorker(
    private val context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val eventNotifyScheduler = EventNotifyScheduler(context)

        TODO("Not yet implemented")
    }
}
