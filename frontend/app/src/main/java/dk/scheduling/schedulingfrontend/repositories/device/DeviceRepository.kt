package dk.scheduling.schedulingfrontend.repositories.device

import dk.scheduling.schedulingfrontend.api.ApiService
import dk.scheduling.schedulingfrontend.api.protocol.CreateDeviceRequest
import dk.scheduling.schedulingfrontend.api.protocol.Device
import dk.scheduling.schedulingfrontend.exceptions.CreationFailedException
import dk.scheduling.schedulingfrontend.exceptions.DeletionFailedException
import dk.scheduling.schedulingfrontend.exceptions.NoBodyWasProvidedException
import dk.scheduling.schedulingfrontend.exceptions.UnauthorizedException
import dk.scheduling.schedulingfrontend.exceptions.UnsuccessfulRequestException
import dk.scheduling.schedulingfrontend.repositories.account.IAccountRepository

class DeviceRepository(
    private val service: ApiService,
    private val accountRepository: IAccountRepository,
) : IDeviceRepository {
    private suspend fun getAuthToken(): String {
        return accountRepository.getAuthToken().toString()
    }

    override suspend fun getAllDevices(): List<Device> {
        val authToken = getAuthToken()
        val response = service.getAllDevices(authToken)
        if (response.isSuccessful) {
            return response.body()?.devices ?: throw NoBodyWasProvidedException("No body was provided", response = response.raw())
        }
        if (response.code() == 401) {
            throw UnauthorizedException("The user is not authorized", authToken = authToken)
        }

        throw UnsuccessfulRequestException("The server couldn't provide a list of devices", response = response.raw())
    }

    override suspend fun createDevice(
        name: String,
        effect: Double,
    ) {
        val authToken = getAuthToken()
        val response = service.createDevice(authToken = authToken, CreateDeviceRequest(name, effect))
        if (response.isSuccessful) {
            return
        }
        if (response.code() == 401) {
            throw UnauthorizedException("The user is not authorized", authToken = authToken)
        }

        throw CreationFailedException("The server couldn't create a device", response = response.raw())
    }

    override suspend fun deleteDevice(deviceId: Long) {
        val authToken = getAuthToken()
        val response = service.deleteDevice(authToken, deviceId)
        if (response.isSuccessful) {
            return
        }
        if (response.code() == 401) {
            throw UnauthorizedException("The user is not authorized", authToken = authToken)
        }

        throw DeletionFailedException("The server couldn't delete a device", response = response.raw())
    }
}
