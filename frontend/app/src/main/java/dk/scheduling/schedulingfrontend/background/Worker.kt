package dk.scheduling.schedulingfrontend.background

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import dk.scheduling.schedulingfrontend.App
import dk.scheduling.schedulingfrontend.database.EventAlarm
import dk.scheduling.schedulingfrontend.eventNotify.EventNotifyScheduler
import dk.scheduling.schedulingfrontend.exceptions.UserNotLoggedInException
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

fun eventCollectWorkOnetime(): OneTimeWorkRequest {
    val constraints =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    return OneTimeWorkRequestBuilder<EventCollectorWorker>()
        .setConstraints(constraints)
        .build()
}

class EventCollectorWorker(
    private val context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        Log.i("EventCollectorWorker", "Run EventCollectorWorker")

        val dao = App.eventAlarmDb.eventAlarmDao()
        val eventNotifyScheduler = EventNotifyScheduler(context)
        val eventsIdStored = dao.getAll().map { it.id }.toSet()
        val retrievedEventIds = mutableSetOf<Long>()

        Log.i("EventCollectorWorker", "Starting retrieving DeviceTasks and Schedule Alarm")
        try {
            App.appModule.overviewRepo.getDeviceTasks().forEach { deviceTask ->
                deviceTask.tasks.forEach { taskEvent ->
                    if (taskEvent.event != null) {
                        val id = taskEvent.event.id
                        Log.i("EventCollectorWorker", "New event $id")
                        retrievedEventIds.add(id)
                        if (!(eventsIdStored.contains(id))) {
                            val eventAlarm =
                                EventAlarm(
                                    id = id,
                                    deviceName = deviceTask.device.name,
                                    startTime = taskEvent.event.start_time,
                                    duration = taskEvent.task.duration,
                                )
                            dao.insert(eventAlarm)
                            eventNotifyScheduler.scheduler(eventAlarm)
                        }
                    }
                }
            }
        } catch (e: UserNotLoggedInException) {
            Log.e("EventCollectorWorker", "UserNotLoggedInException: ${e.message}", e.other_exception)
            return Result.retry()
        } catch (e: Throwable) {
            Log.e("EventCollectorWorker", "Exception: ${e.message}", e)
            return Result.retry()
        }

        val cancelEvents = eventsIdStored - retrievedEventIds
        Log.i("EventCollectorWorker", "Cancel eventAlarms $cancelEvents")
        cancelEvents.forEach {
            dao.loadById(it)?.let {
                eventNotifyScheduler.cancel(it)
                dao.delete(it)
            }
        }

        return Result.success()
    }
}
