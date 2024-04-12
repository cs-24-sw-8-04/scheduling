package testdata

import dk.scheduling.schedulingfrontend.device.Device
import dk.scheduling.schedulingfrontend.device.DeviceOverview
import dk.scheduling.schedulingfrontend.device.Event
import java.time.LocalDateTime

fun testDeviceOverview(): List<DeviceOverview> {
    val dateTimeNow = LocalDateTime.now()

    return mutableListOf(
        DeviceOverview(
            Device(1, "Washing machine 1", 100),
            Event(
                dateTimeNow,
                20 * 60 * 1000,
            ),
        ),
        DeviceOverview(
            Device(2, "Washing machine 2", 40),
            Event(
                dateTimeNow.plusMinutes(20),
                5 * 60 * 1000,
            ),
        ),
        DeviceOverview(
            Device(3, "Car 1", 1000),
            null,
        ),
        DeviceOverview(
            Device(4, "Car 2", 1000),
            Event(
                dateTimeNow.plusMinutes(10),
                5 * 60 * 1000,
            ),
        ),
        DeviceOverview(
            Device(5, "Dishwasher", 23),
            Event(
                dateTimeNow.plusMinutes(30),
                5 * 60 * 1000,
            ),
        ),
        DeviceOverview(
            Device(6, "Washing machine 5", 30),
            null,
        ),
        DeviceOverview(
            Device(7, "Phone Charger", 5),
            Event(
                dateTimeNow.plusMinutes(25),
                5 * 60 * 1000,
            ),
        ),
        DeviceOverview(
            Device(8, "Vacuum Cleaner", 7),
            Event(
                dateTimeNow.plusMinutes(70),
                5 * 60 * 1000,
            ),
        ),
        DeviceOverview(
            Device(9, "Robot green mover", 24),
            null,
        ),
    )
}
