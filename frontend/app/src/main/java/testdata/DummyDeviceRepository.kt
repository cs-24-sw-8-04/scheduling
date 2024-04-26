package testdata

import dk.scheduling.schedulingfrontend.api.protocol.Device
import dk.scheduling.schedulingfrontend.repositories.device.IDeviceRepository

@Suppress("BlockingMethodInNonBlockingContext")
class DummyDeviceRepository : IDeviceRepository {
    private val devices: MutableList<Device> =
        mutableListOf(
            Device(1L, "Washing Machine", 100.0),
            Device(2L, "Toaster", 100.0),
            Device(3L, "Electric car", 100.0),
        )

    private var nextDeviceId: Long = 4
    private val sleepDuration: Long = 2000

    override suspend fun getAllDevices(): List<Device> {
        Thread.sleep(sleepDuration)
        return devices.toList()
    }

    override suspend fun createDevice(
        name: String,
        effect: Double,
    ) {
        Thread.sleep(sleepDuration)
        devices.add(Device(nextDeviceId++, name, effect))
    }

    override suspend fun deleteDevice(deviceId: Long) {
        Thread.sleep(sleepDuration)
        devices.removeAll { device: Device -> device.id == deviceId }
    }
}
