package dk.scheduling.schedulingfrontend

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import dk.scheduling.schedulingfrontend.eventNotify.EventNotification
import dk.scheduling.schedulingfrontend.eventNotify.showNotification
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TestNotification {
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
        val eventNotification = EventNotification(textTitle = "Test", textContent = "TESTTEST", startEvent = false)
        showNotification(
            context = context,
            notification = eventNotification,
        )

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val composeTestRule = createComposeRule()

        composeTestRule.waitUntil { manager.activeNotifications.isNotEmpty() }

        with(manager.activeNotifications.first()) {
            assertEquals(id, this.notification.)
            assertEquals(name, )
        }
    }

    @After
    fun tearDown() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancelAll()
    }
}
