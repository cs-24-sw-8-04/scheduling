package dk.scheduling.schedulingfrontend.repositories.overviews

import dk.scheduling.schedulingfrontend.model.DeviceOverview
import dk.scheduling.schedulingfrontend.model.DeviceTask

interface IOverviewsRepository {
    suspend fun getDeviceOverview(): List<DeviceOverview>

    suspend fun getDeviceTasks(): List<DeviceTask>
}
