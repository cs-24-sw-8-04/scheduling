package dk.scheduling.schedulingfrontend.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dk.scheduling.schedulingfrontend.model.DeviceTask
import dk.scheduling.schedulingfrontend.model.EventTask
import dk.scheduling.schedulingfrontend.ui.theme.SchedulingFrontendTheme
import testdata.deviceTaskTestData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskOverviewPage(
    getDeviceTasks: () -> List<DeviceTask>,
    modifier: Modifier = Modifier,
) {
    var deviceTasks = getDeviceTasks().toMutableStateList()
    val refreshState = rememberPullToRefreshState()
    if (refreshState.isRefreshing) {
        LaunchedEffect(true) {
            deviceTasks = getDeviceTasks().toMutableStateList()
            refreshState.endRefresh()
        }
    }

    Box(Modifier.nestedScroll(refreshState.nestedScrollConnection)) {
        LazyColumn(
            modifier =
                modifier
                    .fillMaxSize(),
        ) {
            items(deviceTasks) { deviceTask ->
                DeviceTaskCard(
                    deviceTask = deviceTask,
                )
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }

        PullToRefreshContainer(
            modifier = Modifier.align(Alignment.TopCenter),
            state = refreshState,
        )
    }
}

@Composable
fun DeviceTaskCard(deviceTask: DeviceTask) {
    Card(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(12.dp),
    ) {
        Text(
            text = deviceTask.device.name,
            textAlign = TextAlign.Center,
            modifier =
                Modifier.fillMaxWidth().padding(8.dp),
        )

        deviceTask.tasks.forEach {
            TaskViewer(taskEvent = it)
        }

        Spacer(Modifier.weight(1f))
    }
}

@Composable
fun TaskViewer(
    modifier: Modifier = Modifier,
    taskEvent: EventTask,
) {
    Card(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(12.dp),
    ) {
        Text(
            text = taskEvent.task.id.toString(),
            textAlign = TextAlign.Center,
            modifier =
                Modifier.fillMaxWidth().padding(8.dp),
        )
        RowScope {
        }
    }
}

@Composable
fun Duration(duration_mills: Double) {
}

@Preview(showBackground = true, device = "spec:id=reference_phone,shape=Normal,width=411,height=891,unit=dp,dpi=420")
@Composable
fun TaskOverviewPagePreviewLightMode() {
    SchedulingFrontendTheme(darkTheme = false, dynamicColor = false) {
        TaskOverviewPage({ deviceTaskTestData() })
    }
}

@Preview(showBackground = false, device = "spec:id=reference_phone,shape=Normal,width=411,height=891,unit=dp,dpi=420")
@Composable
fun TaskOverviewPagePreviewDarkMode() {
    SchedulingFrontendTheme(darkTheme = true, dynamicColor = false) {
        TaskOverviewPage({ deviceTaskTestData() })
    }
}
