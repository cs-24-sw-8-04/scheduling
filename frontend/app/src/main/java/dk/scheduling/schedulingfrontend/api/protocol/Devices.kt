package dk.scheduling.schedulingfrontend.api.protocol

data class Device(
    val id: Long,
    val effect: Double,
    val account_id: Long,
)

data class CreateDeviceRequest(
    val effect: Double,
)

data class DeleteDeviceRequest(
    val id: Long,
)
