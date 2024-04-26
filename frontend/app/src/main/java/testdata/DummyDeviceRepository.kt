package testdata

import dk.scheduling.schedulingfrontend.api.protocol.Device
import dk.scheduling.schedulingfrontend.repositories.device.IDeviceRepository

class DummyDeviceRepository : IDeviceRepository {
    private val devices: MutableList<Device> =
        mutableListOf(
            Device(1L, "Washing Machine", 100.0),
            Device(2L, "Toaster", 100.0),
            Device(3L, "Electric car", 100.0),
        )

    private var nextDeviceId: Long = 4

    override suspend fun getAllDevices(): List<Device> {
        return devices.toList()
    }

    override suspend fun createDevice(
        name: String,
        effect: Double,
    ) {
        devices.add(Device(nextDeviceId++, name, effect))
    }

    override suspend fun deleteDevice(deviceId: Long) {
        devices.removeAll { device: Device -> device.id == deviceId }
    }
}
