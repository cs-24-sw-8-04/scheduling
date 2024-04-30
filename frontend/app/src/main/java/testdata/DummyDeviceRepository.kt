package testdata

import dk.scheduling.schedulingfrontend.api.protocol.Device
import dk.scheduling.schedulingfrontend.exceptions.UserNotLoggedInException
import dk.scheduling.schedulingfrontend.repositories.device.IDeviceRepository
import kotlinx.coroutines.delay

class DummyDeviceRepository(private val sleepDuration: Long = 2000) : IDeviceRepository {
    private val devices: MutableList<Device> = devicesTestData().toMutableList()

    override suspend fun getAllDevices(): List<Device> {
        delay(sleepDuration)
        return devices.toList()
    }

    override suspend fun createDevice(
        name: String,
        effect: Double,
    ) {
        throw UserNotLoggedInException()
        delay(sleepDuration)
        val newDeviceId = devices.maxOf { device -> device.id } + 1
        devices.add(Device(newDeviceId, name, effect))
    }

    override suspend fun deleteDevice(deviceId: Long) {
        delay(sleepDuration)
        devices.removeAll { device: Device -> device.id == deviceId }
    }
}
