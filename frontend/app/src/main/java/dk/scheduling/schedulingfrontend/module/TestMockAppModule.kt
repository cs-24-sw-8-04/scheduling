package dk.scheduling.schedulingfrontend.module

import android.content.Context
import androidx.core.content.ContextCompat
import dk.scheduling.schedulingfrontend.R
import dk.scheduling.schedulingfrontend.datasources.AccountDataSource
import dk.scheduling.schedulingfrontend.datasources.api.ApiService
import dk.scheduling.schedulingfrontend.datasources.api.getApiClient
import dk.scheduling.schedulingfrontend.repositories.account.IAccountRepository
import dk.scheduling.schedulingfrontend.repositories.device.IDeviceRepository
import dk.scheduling.schedulingfrontend.repositories.event.IEventRepository
import dk.scheduling.schedulingfrontend.repositories.overviews.OverviewRepository
import dk.scheduling.schedulingfrontend.repositories.task.ITaskRepository
import testdata.DummyAccountRepository
import testdata.DummyDeviceRepository
import testdata.DummyEventRepository
import testdata.DummyTaskRepository

class TestMockAppModule(
    private val context: Context,
) : IAppModule {
    override val apiService: ApiService by lazy {
        getApiClient(baseUrl = ContextCompat.getString(context, R.string.base_url))
    }
    override val accountDataStorage: AccountDataSource by lazy {
        AccountDataSource(context)
    }
    override val accountRepo: IAccountRepository by lazy {
        DummyAccountRepository()
    }
    override val deviceRepo: IDeviceRepository by lazy {
        DummyDeviceRepository(0)
    }
    override val taskRepo: ITaskRepository by lazy {
        DummyTaskRepository(0)
    }
    override val eventRepo: IEventRepository by lazy {
        DummyEventRepository(0)
    }
    override val overviewRepo: OverviewRepository by lazy {
        OverviewRepository(
            deviceRepository = deviceRepo,
            taskRepository = taskRepo,
            eventRepository = eventRepo,
        )
    }
}
