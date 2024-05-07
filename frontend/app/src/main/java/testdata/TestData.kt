package testdata

import dk.scheduling.schedulingfrontend.datasources.api.protocol.Device
import dk.scheduling.schedulingfrontend.datasources.api.protocol.Event
import dk.scheduling.schedulingfrontend.datasources.api.protocol.Task
import dk.scheduling.schedulingfrontend.datasources.api.protocol.Timespan
import java.time.LocalDateTime

fun devicesTestData(): List<Device> {
    return mutableListOf(
        Device(1, "Washing machine 1", 100.0),
        Device(2, "Washing machine 2", 40.0),
        Device(3, "Car 1", 1000.0),
        Device(4, "Car 2", 1000.0),
        Device(5, "Dishwasher", 23.0),
        Device(6, "Washing machine 5", 30.0),
        Device(7, "Phone Charger", 5.0),
        Device(8, "Vacuum Cleaner", 7.0),
        Device(9, "Robot green mover", 24.0),
    )
}

fun tasksTestData(dateTime: LocalDateTime = LocalDateTime.now()): List<Task> {
    return mutableListOf(
        Task(1, Timespan(dateTime.minusMinutes(5), dateTime.plusSeconds(20 * 60)), 20 * 60 * 1000, 1),
        Task(10, Timespan(dateTime.plusMinutes(100), dateTime.plusMinutes(100).plusSeconds(20 * 60).plusMinutes(20)), 20 * 60 * 1000, 1),
        Task(2, Timespan(dateTime, dateTime.plusSeconds(5 * 60).plusMinutes(20)), 5 * 60 * 1000, 2),
        Task(4, Timespan(dateTime, dateTime.plusSeconds(5 * 60).plusMinutes(20)), 5 * 60 * 1000, 4),
        Task(5, Timespan(dateTime.plusMinutes(15), dateTime.plusSeconds(30 * 60).plusMinutes(20)), 30 * 60 * 1000, 5),
        Task(7, Timespan(dateTime.plusMinutes(20), dateTime.plusSeconds(5 * 60).plusMinutes(20)), 5 * 60 * 1000, 7),
        Task(8, Timespan(dateTime.plusMinutes(60), dateTime.plusSeconds(5 * 60).plusMinutes(20)), 5 * 60 * 1000, 8),
    )
}

fun eventsTestData(dateTimeNow: LocalDateTime = LocalDateTime.now()): List<Event> {
    return mutableListOf(
        Event(
            1,
            1,
            dateTimeNow,
        ),
        Event(
            2,
            2,
            dateTimeNow.plusMinutes(20),
        ),
        Event(
            3,
            4,
            dateTimeNow.plusMinutes(10),
        ),
        Event(
            4,
            5,
            dateTimeNow.plusMinutes(30),
        ),
        Event(
            5,
            7,
            dateTimeNow.plusMinutes(25),
        ),
        Event(
            6,
            8,
            dateTimeNow.plusMinutes(70),
        ),
    )
}
