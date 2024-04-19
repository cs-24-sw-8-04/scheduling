@file:Suppress("PropertyName")

package dk.scheduling.schedulingfrontend.api.protocol

data class Device(
    val id: Long,
    val name: String,
    val effect: Double, // Watts
)

data class GetDevicesResponse(
    val devices: List<Device>,
)

data class CreateDeviceRequest(
    val name: String,
    val effect: Double, // Watts
)

data class CreateDeviceResponse(
    val device: Device,
)
