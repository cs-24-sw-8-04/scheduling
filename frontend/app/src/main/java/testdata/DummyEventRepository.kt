package testdata

import dk.scheduling.schedulingfrontend.datasources.api.protocol.Event
import dk.scheduling.schedulingfrontend.repositories.event.IEventRepository
import kotlinx.coroutines.delay
import java.time.LocalDateTime

class DummyEventRepository(private val sleepDuration: Long = 2000) : IEventRepository {
    private val events = eventsTestData(LocalDateTime.now()).toMutableList()

    override suspend fun getAllEvents(): List<Event> {
        delay(sleepDuration)
        return events
    }
}
