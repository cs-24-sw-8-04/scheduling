package dk.scheduling.schedulingfrontend

import dk.scheduling.schedulingfrontend.repositories.overviews.OverviewRepository
import kotlinx.coroutines.runBlocking
import org.junit.Test
import testdata.DummyDeviceRepository
import testdata.DummyEventRepository
import testdata.DummyTaskRepository
import testdata.testDeviceOverview

class OverviewRepositoryTest {
    @Test
    fun getDeviceOverviewTestWithDummyData() {
        val overviewRepository = OverviewRepository(DummyDeviceRepository(), DummyTaskRepository(), DummyEventRepository())
        runBlocking {
            val deviceOverview = overviewRepository.getDeviceOverview()
            val deviceOverviewTest = testDeviceOverview()
            assert(!deviceOverview.isEmpty())
            assert(!deviceOverviewTest.isEmpty())
            assert(deviceOverview == deviceOverviewTest)
        }
    }
}
