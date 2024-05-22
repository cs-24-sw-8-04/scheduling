package dk.scheduling.schedulingfrontend.repositories.event

import dk.scheduling.schedulingfrontend.datasources.api.protocol.Event

interface IEventRepository {
    suspend fun getAllEvents(): List<Event>
}
