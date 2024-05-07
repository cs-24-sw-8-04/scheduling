package dk.scheduling.schedulingfrontend

import android.content.Context
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import dk.scheduling.schedulingfrontend.background.EventAlarmSetterWorker
import dk.scheduling.schedulingfrontend.background.eventAlarmSetterWorkPeriodicRequest
import dk.scheduling.schedulingfrontend.module.TestMockAppModule
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventWorkerTest {
    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val config =
            Configuration.Builder()
                .setMinimumLoggingLevel(Log.DEBUG)
                .setExecutor(SynchronousExecutor())
                .build()

        // Initialize WorkManager for instrumentation tests.
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
    }

    @Test
    @Throws(Exception::class)
    fun testPeriodicEventCollectorWork() {
        // Create request
        val request = eventAlarmSetterWorkPeriodicRequest(java.time.Duration.ofMinutes(15))
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        App.appModule = TestMockAppModule(context)
        val workManager = WorkManager.getInstance(context)
        val testDriver = WorkManagerTestInitHelper.getTestDriver(context) ?: return fail("TestDriver is null")
        // Enqueue and wait for result.
        workManager.enqueue(request).result.get()
        // Tells the testing framework the period delay is met
        testDriver.setPeriodDelayMet(request.id)
        // Get WorkInfo and outputData
        val workInfo = workManager.getWorkInfoById(request.id).get()

        // Assert
        assertEquals(WorkInfo.State.ENQUEUED, workInfo.state)
    }

    @Test
    @Throws(Exception::class)
    fun testEventAlarmSet() {
        // Create request
        val request = eventAlarmSetterWorkPeriodicRequest(java.time.Duration.ofMinutes(15))
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        App.appModule = TestMockAppModule(context)

        val db = App.eventAlarmDb

        assertTrue("the list suppose to be empty", db.eventAlarmDao().getAll().isEmpty())
        runBlocking {
            val expectedNumEvents = App.appModule.eventRepo.getAllEvents().count()

            val workManager = WorkManager.getInstance(context)
            val testDriver = WorkManagerTestInitHelper.getTestDriver(context) ?: return@runBlocking fail("TestDriver is null")

            // Enqueue and wait for result.
            workManager.enqueue(request).result.get()

            // Tells the testing framework the period delay is met
            testDriver.setPeriodDelayMet(request.id)

            // Get WorkInfo and outputData
            val workInfo = workManager.getWorkInfoById(request.id).get()

            val actualNumEventAlarms = db.eventAlarmDao().getAll().count()

            // Assert
            assertEquals(WorkInfo.State.ENQUEUED, workInfo.state)
            assertEquals(expectedNumEvents, actualNumEventAlarms)
        }
    }
}

@RunWith(AndroidJUnit4::class)
class EventAlarmWorkerTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        App.appModule = TestMockAppModule(context)
    }

    @Test
    fun testWorkerSetsEventAlarm() {
        val db = App.eventAlarmDb
        assertTrue("The list suppose to be empty", db.eventAlarmDao().getAll().isEmpty())
        runBlocking {
            val expectedNumEvents = App.appModule.eventRepo.getAllEvents().count()
            val worker = TestListenableWorkerBuilder<EventAlarmSetterWorker>(context).build()

            val result = worker.doWork()

            // The localDB must have filled with eventAlarms
            val actualNumEventAlarms = db.eventAlarmDao().getAll().count()

            assertEquals(ListenableWorker.Result.success(), result)
            assertEquals(expectedNumEvents, actualNumEventAlarms)
        }
    }
}
