package dk.scheduling.schedulingfrontend.repositories.task

import android.content.Context
import androidx.work.WorkManager
import dk.scheduling.schedulingfrontend.api.ApiService
import dk.scheduling.schedulingfrontend.api.protocol.CreateTaskRequest
import dk.scheduling.schedulingfrontend.api.protocol.Task
import dk.scheduling.schedulingfrontend.api.protocol.Timespan
import dk.scheduling.schedulingfrontend.background.eventCollectWorkOnetime
import dk.scheduling.schedulingfrontend.exceptions.CreationFailedException
import dk.scheduling.schedulingfrontend.exceptions.DeletionFailedException
import dk.scheduling.schedulingfrontend.exceptions.NoBodyWasProvidedException
import dk.scheduling.schedulingfrontend.exceptions.UnauthorizedException
import dk.scheduling.schedulingfrontend.exceptions.UnsuccessfulRequestException
import dk.scheduling.schedulingfrontend.repositories.account.IAccountRepository

class TaskRepository(
    private val service: ApiService,
    private val accountRepository: IAccountRepository,
    private val context: Context,
) : ITaskRepository {
    private suspend fun getAuthToken(): String {
        return accountRepository.getAuthToken().toString()
    }

    override suspend fun getAllTasks(): List<Task> {
        val authToken = getAuthToken()
        val response = service.getAllTasks(authToken)
        if (response.isSuccessful) {
            return response.body()?.tasks ?: throw NoBodyWasProvidedException("No body was provided", response = response.raw())
        }
        if (response.code() == 401) {
            throw UnauthorizedException("The user is not authorized", authToken = authToken)
        }

        throw UnsuccessfulRequestException("The server couldn't provide a list of task", response = response.raw())
    }

    override suspend fun createTask(
        timeSpan: Timespan,
        duration: Long,
        device_id: Long,
    ) {
        val authToken = getAuthToken()
        val response = service.createTask(authToken = authToken, CreateTaskRequest(timeSpan, duration, device_id))
        if (response.isSuccessful) {
            val workManager = WorkManager.getInstance(context)
            workManager.enqueue(eventCollectWorkOnetime())
            return
        }
        if (response.code() == 401) {
            throw UnauthorizedException("The user is not authorized", authToken = authToken)
        }

        throw CreationFailedException("The server couldn't create a task", response = response.raw())
    }

    override suspend fun deleteTask(taskId: Long) {
        val authToken = getAuthToken()
        val response = service.deleteTask(authToken, taskId)
        if (response.isSuccessful) {
            return
        }
        if (response.code() == 401) {
            throw UnauthorizedException("The user is not authorized", authToken = authToken)
        }

        throw DeletionFailedException("The server couldn't delete a task", response = response.raw())
    }
}
