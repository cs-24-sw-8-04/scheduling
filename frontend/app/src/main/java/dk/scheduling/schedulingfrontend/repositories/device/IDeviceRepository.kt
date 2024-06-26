package dk.scheduling.schedulingfrontend.repositories.device

import dk.scheduling.schedulingfrontend.datasources.api.protocol.Device

interface IDeviceRepository {
    suspend fun getAllDevices(): List<Device>

    suspend fun createDevice(
        name: String,
        effect: Double,
    )

    suspend fun deleteDevice(deviceId: Long)
}
