package dk.scheduling.schedulingfrontend

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import dk.scheduling.schedulingfrontend.eventNotify.EventNotification
import dk.scheduling.schedulingfrontend.eventNotify.showNotification
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
class TestNotification {
    @get:Rule
    val composeTestRule = createComposeRule()
    val TAG = "TestNotification"

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
    fun checkNotificationSend() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val notificationId = Random.nextInt()
        Log.d(TAG, "checkNotificationSend: notificationId = $notificationId")

        val eventNotification = EventNotification(textTitle = "Test", textContent = "TESTTEST", startEvent = false)

        showNotification(
            notificationId = notificationId,
            context = context,
            notification = eventNotification,
        )
        Log.d(TAG, "checkNotificationSend: Showing notification = $eventNotification")

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val composeTestRule = createComposeRule()

        composeTestRule.waitUntil { manager.activeNotifications.isNotEmpty() }

        with(manager.activeNotifications.first()) {
            assertEquals(notificationId, this.notification)
        }
    }

    @After
    fun tearDown() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancelAll()
    }
}
