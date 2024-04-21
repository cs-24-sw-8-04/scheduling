package dk.scheduling.schedulingfrontend.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
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
import dk.scheduling.schedulingfrontend.model.DeviceTask
import dk.scheduling.schedulingfrontend.model.TaskEvent
import dk.scheduling.schedulingfrontend.ui.theme.SchedulingFrontendTheme
import testdata.deviceTaskTestData
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

val dateFormat = DateTimeFormatter.ofPattern("MMM dd", Locale.ENGLISH)
val timeFormat = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
val dateAndTimeFormat = DateTimeFormatter.ofPattern("MMM dd hh:mm a", Locale.ENGLISH)

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
                if (deviceTask.tasks.isNotEmpty()) {
                    DeviceTaskCard(
                        deviceTask = deviceTask,
                    ) { deviceTasks.remove(deviceTask) }
                }
            }
        }

        PullToRefreshContainer(
            modifier = Modifier.align(Alignment.TopCenter),
            state = refreshState,
        )
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
                .fillMaxSize()
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
                .fillMaxSize()
                .padding(horizontal = 12.dp).padding(bottom = 12.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row {
                    Duration(taskEvent.task.duration)
                    Interval(taskEvent.task.timespan)
                }
                TaskMenu(
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
        Text(text = "Scheduled to start " + event.start_time.format(dateAndTimeFormat))
    } else {
        Text(text = "Not scheduled yet")
    }
}

@Composable
fun Duration(durationMills: Long) {
    val displayText = @Composable
    fun (text: String, fontSize: Float) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            fontSize = TextUnit(fontSize, TextUnitType.Sp),
            modifier =
            Modifier.fillMaxWidth(),
        )
    }

    val fontSizeNumber = 25f
    val fontSizeUnitLabel = 12f
    val (hours, minutes) = milliSecondToHourMinute(durationMills)

    Column(
        modifier =
            Modifier.fillMaxWidth(0.35f),
    ) {
        SectionTitleLabel("Duration")

        Row {
            if (hours != 0L) {
                Column(
                    modifier =
                        Modifier.fillMaxWidth(),
                ) {
                    displayText("$hours", fontSizeNumber)
                    displayText("hr", fontSizeUnitLabel)
                }
            }

            Column(
                modifier =
                    Modifier.fillMaxWidth(if (hours != 0L) 0.35f else 1f),
            ) {
                displayText("$minutes", fontSizeNumber)
                displayText("min", fontSizeUnitLabel)
            }
        }
    }
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
            modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
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
        modifier = Modifier.fillMaxHeight().padding(vertical = 12.dp),
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
            text = dateTime.format(dateFormat),
            textAlign = TextAlign.Center,
            fontSize = TextUnit(20f, TextUnitType.Sp),
        )
        Text(
            text = dateTime.format(timeFormat),
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
fun TaskMenu(onRemove: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().padding(start = 10.dp).wrapContentSize(Alignment.TopStart),
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
                text = { Text("Remove") },
                onClick = {
                    // Handle remove!
                    expanded = false
                    onRemove()
                },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = "Remove task",
                    )
                },
            )
        }
    }
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
