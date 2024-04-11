package dk.scheduling.schedulingfrontend.device

import java.time.LocalDateTime

data class Device(
    var id: Int,
    var name: String,
    var effect: Int?,
)

data class DeviceOverview(
    var device: Device,
    var event: Event?,
)

data class Event(
    var startTime: LocalDateTime,
    var duration: Long,
)

fun testDeviceOverview(): List<DeviceOverview> {
    return mutableListOf(
        DeviceOverview(
            Device(1, "Washing machine 1", 100),
            Event(
                LocalDateTime.now(),
                20 * 60 * 1000,
            ),
        ),
        DeviceOverview(
            Device(2, "Washing machine 2", 40),
            Event(
                LocalDateTime.now().plusMinutes(20),
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
                LocalDateTime.now().plusMinutes(10),
                5 * 60 * 1000,
            ),
        ),
        DeviceOverview(
            Device(5, "Dishwasher", 23),
            Event(
                LocalDateTime.now().plusMinutes(30),
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
                LocalDateTime.now().plusMinutes(25),
                5 * 60 * 1000,
            ),
        ),
        DeviceOverview(
            Device(8, "Vacuum Cleaner", 7),
            Event(
                LocalDateTime.now().plusMinutes(70),
                5 * 60 * 1000,
            ),
        ),
        DeviceOverview(
            Device(9, "Robot green mover", 24),
            null,
        ),
    )
}
