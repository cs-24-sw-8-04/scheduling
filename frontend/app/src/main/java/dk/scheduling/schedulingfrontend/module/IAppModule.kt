package dk.scheduling.schedulingfrontend.module

import dk.scheduling.schedulingfrontend.datasources.AccountDataSource
import dk.scheduling.schedulingfrontend.datasources.api.ApiService
import dk.scheduling.schedulingfrontend.repositories.account.IAccountRepository
import dk.scheduling.schedulingfrontend.repositories.device.IDeviceRepository
import dk.scheduling.schedulingfrontend.repositories.event.IEventRepository
import dk.scheduling.schedulingfrontend.repositories.overviews.OverviewRepository
import dk.scheduling.schedulingfrontend.repositories.task.ITaskRepository

interface IAppModule {
    val apiService: ApiService
    val accountDataStorage: AccountDataSource
    val accountRepo: IAccountRepository
    val deviceRepo: IDeviceRepository
    val taskRepo: ITaskRepository
    val eventRepo: IEventRepository
    val overviewRepo: OverviewRepository
}
