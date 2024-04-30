package dk.scheduling.schedulingfrontend.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import dk.scheduling.schedulingfrontend.api.protocol.Event
import dk.scheduling.schedulingfrontend.api.protocol.Timespan
import dk.scheduling.schedulingfrontend.components.ConfirmAlertDialog
import dk.scheduling.schedulingfrontend.components.DATE_AND_TIME_FORMAT
import dk.scheduling.schedulingfrontend.components.DATE_FORMAT
import dk.scheduling.schedulingfrontend.components.Loading
import dk.scheduling.schedulingfrontend.components.TIME_FORMAT
import dk.scheduling.schedulingfrontend.model.DeviceTask
import dk.scheduling.schedulingfrontend.model.TaskEvent
import dk.scheduling.schedulingfrontend.repositories.overviews.OverviewRepository
import dk.scheduling.schedulingfrontend.ui.theme.SchedulingFrontendTheme
import testdata.DummyDeviceRepository
import testdata.DummyEventRepository
import testdata.DummyTaskRepository
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskOverviewPage(
    modifier: Modifier = Modifier,
    overviewRepository: OverviewRepository,
) {
    var deviceTasks by remember { mutableStateOf(mutableListOf<DeviceTask>()) }
    val refreshState = rememberPullToRefreshState()
    if (refreshState.isRefreshing) {
        LaunchedEffect(true) {
            deviceTasks = overviewRepository.getDeviceTasks().toMutableList()
            refreshState.endRefresh()
        }
    }

    val (isLoading, setIsLoading) = remember { mutableStateOf(true) }

    Loading(
        isLoading = isLoading,
        setIsLoading = setIsLoading,
        onLoading = { deviceTasks = overviewRepository.getDeviceTasks().toMutableList() },
    ) {
        Box(Modifier.nestedScroll(refreshState.nestedScrollConnection)) {
            LazyColumn(
                modifier =
                    modifier
                        .fillMaxSize(),
            ) {
                items(deviceTasks) { deviceTask ->
                    if (deviceTask.tasks.isNotEmpty()) {
                        DeviceTaskCard(
                            deviceTask = deviceTask,
                        ) { deviceTasks.remove(deviceTask) }
                    }
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
}

@Composable
fun DeviceTaskCard(
    deviceTask: DeviceTask,
    onRemoveDeviceTask: () -> Unit,
) {
    val tasks = deviceTask.tasks.toMutableStateList()

    Card(
        modifier =
            Modifier
                .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text = deviceTask.device.name,
            textAlign = TextAlign.Center,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
        )

        tasks.forEach {
            TaskViewer(
                taskEvent = it,
            ) { tasks.remove(it) }
        }

        if (tasks.isEmpty()) {
            onRemoveDeviceTask()
        }
    }
}

@Composable
fun TaskViewer(
    taskEvent: TaskEvent,
    onRemove: () -> Unit,
) {
    Card(
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceBright,
            ),
        modifier =
            Modifier
                .padding(horizontal = 12.dp)
                .padding(bottom = 12.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row {
                    Duration(taskEvent.task.duration)
                    Interval(taskEvent.task.timespan)
                }
                TaskMenu(
                    taskEvent = taskEvent,
                    onRemove = onRemove,
                )
            }
            TaskScheduled(taskEvent.event)
        }
    }
}

@Composable
fun TaskScheduled(event: Event?) {
    if (event != null) {
        Text(text = "Scheduled to start " + event.start_time.format(DATE_AND_TIME_FORMAT))
    } else {
        Text(text = "Not scheduled yet")
    }
}

@Composable
fun Duration(durationMills: Long) {
    val fontSizeNumber = 25f
    val fontSizeUnitLabel = 12f
    val (hours, minutes) = milliSecondToHourMinute(durationMills)

    Column(
        modifier =
            Modifier.fillMaxWidth(0.30f),
    ) {
        SectionTitleLabel("Duration")

        Row {
            if (hours != 0L) {
                Column(
                    modifier =
                        Modifier.fillMaxWidth(0.5f),
                ) {
                    DisplayText("$hours", fontSizeNumber)
                    DisplayText("hr", fontSizeUnitLabel)
                }
            }

            Column(
                modifier =
                    Modifier.fillMaxWidth(),
            ) {
                DisplayText("$minutes", fontSizeNumber)
                DisplayText("min", fontSizeUnitLabel)
            }
        }
    }
}

@Composable
fun DisplayText(
    text: String,
    fontSize: Float,
) {
    Text(
        text = text,
        textAlign = TextAlign.Center,
        fontSize = TextUnit(fontSize, TextUnitType.Sp),
        modifier = Modifier.fillMaxWidth(),
    )
}

fun milliSecondToHourMinute(millis: Long): Pair<Long, Long> {
    val hours = millis / 3600000 // 1 hour = 3600000 milliseconds
    val minutes = (millis % 3600000) / 60000 // 1 minute = 60000 milliseconds
    return Pair(hours, minutes)
}

@Composable
fun Interval(timeSpan: Timespan) {
    Column(
        modifier = Modifier.fillMaxSize(0.85f),
        verticalArrangement = Arrangement.Center,
    ) {
        SectionTitleLabel("Interval")
        Row(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            DateAndTimeViewer(dateTime = timeSpan.start)
            Dash()
            DateAndTimeViewer(dateTime = timeSpan.end)
        }
    }
}

@Composable
fun Dash() {
    Text(
        modifier =
            Modifier
                .fillMaxHeight()
                .padding(vertical = 12.dp),
        textAlign = TextAlign.Center,
        fontWeight = FontWeight(500),
        fontSize = TextUnit(20f, TextUnitType.Sp),
        text = "-",
    )
}

@Composable
fun DateAndTimeViewer(dateTime: LocalDateTime) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxHeight(),
    ) {
        Text(
            text = dateTime.format(TIME_FORMAT),
            textAlign = TextAlign.Center,
            fontSize = TextUnit(18f, TextUnitType.Sp),
        )
        Text(
            text = dateTime.format(DATE_FORMAT),
            textAlign = TextAlign.Center,
            fontSize = TextUnit(15f, TextUnitType.Sp),
        )
    }
}

@Composable
fun SectionTitleLabel(label: String) {
    Text(
        text = label,
        fontSize = TextUnit(13f, TextUnitType.Sp),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
fun TaskMenu(
    taskEvent: TaskEvent,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var openConfirmDialog by remember { mutableStateOf(false) }
    CancelTaskAlertDialog(
        taskEvent = taskEvent,
        onRemoveTask = onRemove,
        openConfirmDialog = openConfirmDialog,
        setOpenConfirmDialog = { openConfirmDialog = it },
    )

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(start = 10.dp)
                .wrapContentSize(Alignment.TopStart),
    ) {
        var expanded by remember { mutableStateOf(false) }
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "Task menu",
            modifier = Modifier.clickable(onClick = { expanded = true }),
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("Cancel") },
                onClick = {
                    // Handle remove!
                    expanded = false
                    openConfirmDialog = true
                },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Cancel,
                        contentDescription = "Cancel task",
                    )
                },
            )
        }
    }
}

@Composable
fun CancelTaskAlertDialog(
    taskEvent: TaskEvent,
    onRemoveTask: () -> Unit,
    openConfirmDialog: Boolean,
    setOpenConfirmDialog: (Boolean) -> Unit,
) {
    val timeSpan = taskEvent.task.timespan
    val interval = "${timeSpan.start.format(DATE_FORMAT)} - ${timeSpan.end.format(DATE_FORMAT)}"
    val (hour, minute) = milliSecondToHourMinute(taskEvent.task.duration)
    val duration = if (hour > 0) hour.toString() else "$hour hr" + if (minute > 0) "$minute min" else ""

    ConfirmAlertDialog(
        openConfirmDialog = openConfirmDialog,
        setOpenConfirmDialog = setOpenConfirmDialog,
        title = "Cancel Task",
        text = "Want to cancel the task happening $interval with duration $duration",
        confirmLabel = "Yes",
        onConfirm = { onRemoveTask() },
        dismissLabel = "No",
    )
}

@Preview(showBackground = true, device = "spec:id=reference_phone,shape=Normal,width=411,height=891,unit=dp,dpi=420")
@Composable
fun TaskOverviewPagePreviewLightMode() {
    SchedulingFrontendTheme(darkTheme = false, dynamicColor = false) {
        TaskOverviewPage(overviewRepository = OverviewRepository(DummyDeviceRepository(), DummyTaskRepository(), DummyEventRepository()))
    }
}

@Preview(showBackground = false, device = "spec:id=reference_phone,shape=Normal,width=411,height=891,unit=dp,dpi=420")
@Composable
fun TaskOverviewPagePreviewDarkMode() {
    SchedulingFrontendTheme(darkTheme = true, dynamicColor = false) {
        TaskOverviewPage(overviewRepository = OverviewRepository(DummyDeviceRepository(), DummyTaskRepository(), DummyEventRepository()))
    }
}
