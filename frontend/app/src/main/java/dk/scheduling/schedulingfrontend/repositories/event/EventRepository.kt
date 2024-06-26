package dk.scheduling.schedulingfrontend.repositories.event

import android.content.Context
import androidx.work.WorkManager
import dk.scheduling.schedulingfrontend.background.eventnotification.EventAlarmSetterWorker
import dk.scheduling.schedulingfrontend.datasources.api.ApiService
import dk.scheduling.schedulingfrontend.datasources.api.protocol.Event
import dk.scheduling.schedulingfrontend.exceptions.NoBodyWasProvidedException
import dk.scheduling.schedulingfrontend.exceptions.UnauthorizedException
import dk.scheduling.schedulingfrontend.exceptions.UnsuccessfulRequestException
import dk.scheduling.schedulingfrontend.repositories.account.IAccountRepository

class EventRepository(
    private val service: ApiService,
    private val accountRepository: IAccountRepository,
    private val context: Context,
) : IEventRepository {
    private suspend fun getAuthToken(): String {
        return accountRepository.getAuthToken().toString()
    }

    override suspend fun getAllEvents(): List<Event> {
        val authToken = getAuthToken()
        val response = service.getAllEvents(authToken)

        val workManager = WorkManager.getInstance(context)
        workManager.enqueue(EventAlarmSetterWorker.eventAlarmSetterWorkOnetimeRequest())

        if (response.isSuccessful) {
            return response.body()?.events ?: throw NoBodyWasProvidedException("No body was provided", response = response.raw())
        }
        if (response.code() == 401) {
            throw UnauthorizedException("The user is not authorized", authToken = authToken)
        }

        throw UnsuccessfulRequestException("The server couldn't provide a list of events", response = response.raw())
    }
}
