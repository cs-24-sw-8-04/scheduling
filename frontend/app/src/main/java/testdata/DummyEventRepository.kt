package testdata

import dk.scheduling.schedulingfrontend.api.protocol.Event
import dk.scheduling.schedulingfrontend.repositories.event.IEventRepository
import java.time.LocalDateTime

class DummyEventRepository : IEventRepository {
    private val events = eventsTestData(LocalDateTime.now()).toMutableList()
    override suspend fun getAllEvents(): List<Event> {
        return events;
    }
}
