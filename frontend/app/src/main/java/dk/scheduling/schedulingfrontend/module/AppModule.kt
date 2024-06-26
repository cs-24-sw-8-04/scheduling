package dk.scheduling.schedulingfrontend.module

import android.content.Context
import androidx.core.content.ContextCompat.getString
import dk.scheduling.schedulingfrontend.R
import dk.scheduling.schedulingfrontend.datasources.AccountDataSource
import dk.scheduling.schedulingfrontend.datasources.api.ApiService
import dk.scheduling.schedulingfrontend.datasources.api.getApiClient
import dk.scheduling.schedulingfrontend.repositories.account.AccountRepository
import dk.scheduling.schedulingfrontend.repositories.account.IAccountRepository
import dk.scheduling.schedulingfrontend.repositories.device.DeviceRepository
import dk.scheduling.schedulingfrontend.repositories.device.IDeviceRepository
import dk.scheduling.schedulingfrontend.repositories.event.EventRepository
import dk.scheduling.schedulingfrontend.repositories.event.IEventRepository
import dk.scheduling.schedulingfrontend.repositories.overviews.OverviewRepository
import dk.scheduling.schedulingfrontend.repositories.task.ITaskRepository
import dk.scheduling.schedulingfrontend.repositories.task.TaskRepository

class AppModule(
    private val context: Context,
) : IAppModule {
    override val apiService: ApiService by lazy {
        getApiClient(baseUrl = getString(context, R.string.base_url))
    }
    override val accountDataStorage: AccountDataSource by lazy {
        AccountDataSource(context)
    }
    override val accountRepo: IAccountRepository by lazy {
        AccountRepository(accountDataSource = accountDataStorage, service = apiService)
    }
    override val deviceRepo: IDeviceRepository by lazy {
        DeviceRepository(accountRepository = accountRepo, service = apiService)
    }
    override val taskRepo: ITaskRepository by lazy {
        TaskRepository(accountRepository = accountRepo, service = apiService)
    }
    override val eventRepo: IEventRepository by lazy {
        EventRepository(service = apiService, accountRepository = accountRepo, context = context)
    }
    override val overviewRepo: OverviewRepository by lazy {
        OverviewRepository(
            deviceRepository = deviceRepo,
            taskRepository = taskRepo,
            eventRepository = eventRepo,
        )
    }
}
