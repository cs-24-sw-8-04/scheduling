package dk.scheduling.schedulingfrontend.repositories.event

import dk.scheduling.schedulingfrontend.api.ApiService
import dk.scheduling.schedulingfrontend.api.protocol.Event
import dk.scheduling.schedulingfrontend.exceptions.NoBodyWasProvidedException
import dk.scheduling.schedulingfrontend.exceptions.UnauthorizedException
import dk.scheduling.schedulingfrontend.exceptions.UnsuccessfulRequestException
import dk.scheduling.schedulingfrontend.repositories.account.AccountRepository

class EventRepository(
    private val service: ApiService,
    private val accountRepository: AccountRepository,
) : IEventRepository {
    private suspend fun getAuthToken(): String {
        return accountRepository.getAuthToken().toString()
    }

    override suspend fun getAllEvents(): List<Event> {
        val authToken = getAuthToken()
        val response = service.getAllEvents(authToken)
        if (response.isSuccessful) {
            return response.body()?.events ?: throw NoBodyWasProvidedException("No body was provided", response = response.raw())
        }
        if (response.code() == 401) {
            throw UnauthorizedException("The user is not authorized", authToken = authToken)
        }

        throw UnsuccessfulRequestException("The server couldn't provide a list of events", response = response.raw())
    }
}