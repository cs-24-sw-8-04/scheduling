package dk.scheduling.schedulingfrontend.api.protocol

data class Device(
    val id: Long,
    val name: String,
    // Watts
    val effect: Double,
)

data class GetDevicesResponse(
    val devices: List<Device>,
)

data class CreateDeviceRequest(
    val name: String,
    // Watts
    val effect: Double,
)

data class CreateDeviceResponse(
    val device: Device,
)
