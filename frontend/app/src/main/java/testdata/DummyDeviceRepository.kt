package testdata

import dk.scheduling.schedulingfrontend.api.protocol.Device
import dk.scheduling.schedulingfrontend.repositories.device.IDeviceRepository
import kotlinx.coroutines.delay

class DummyDeviceRepository : IDeviceRepository {
    private val devices: MutableList<Device> =
        mutableListOf(
            Device(1L, "Washing Machine", 100.0),
            Device(2L, "Toaster", 100.0),
            Device(3L, "Electric car", 100.0),
        )

    private val sleepDuration: Long = 2000

    override suspend fun getAllDevices(): List<Device> {
        delay(sleepDuration)
        return devices.toList()
    }

    override suspend fun createDevice(
        name: String,
        effect: Double,
    ) {
        delay(sleepDuration)
        val newDeviceId = devices.maxOf { device -> device.id } + 1
        devices.add(Device(newDeviceId, name, effect))
    }

    override suspend fun deleteDevice(deviceId: Long) {
        delay(sleepDuration)
        devices.removeAll { device: Device -> device.id == deviceId }
    }
}
