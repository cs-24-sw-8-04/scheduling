package dk.scheduling.schedulingfrontend.repositories.device

import dk.scheduling.schedulingfrontend.api.protocol.Device

interface IDeviceRepository {
    suspend fun getAllDevices(): List<Device>

    suspend fun createDevice(
        name: String,
        effect: Double,
    )

    suspend fun deleteDevice(taskId: Long)
}
