package dk.scheduling.schedulingfrontend.pages

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dk.scheduling.schedulingfrontend.api.protocol.Device
import dk.scheduling.schedulingfrontend.components.DATE_FORMATTER
import dk.scheduling.schedulingfrontend.components.FilledButton
import dk.scheduling.schedulingfrontend.components.OutlinedButton
import dk.scheduling.schedulingfrontend.model.DeviceOverview
import dk.scheduling.schedulingfrontend.model.DeviceState
import dk.scheduling.schedulingfrontend.model.getDeviceState
import dk.scheduling.schedulingfrontend.ui.theme.SchedulingFrontendTheme
import dk.scheduling.schedulingfrontend.ui.theme.scheduled
import dk.scheduling.schedulingfrontend.ui.theme.success
import testdata.testDeviceOverview
import java.time.temporal.ChronoUnit

@Preview(showBackground = true, device = "spec:id=reference_phone,shape=Normal,width=411,height=891,unit=dp,dpi=420")
@Composable
fun HomePagePreviewLightMode() {
    SchedulingFrontendTheme(darkTheme = false, dynamicColor = false) {
        HomePage(getDevices = { testDeviceOverview() })
    }
}

@Preview(showBackground = false, device = "spec:id=reference_phone,shape=Normal,width=411,height=891,unit=dp,dpi=420")
@Composable
fun HomePagePreviewDarkMode() {
    SchedulingFrontendTheme(darkTheme = true, dynamicColor = false) {
        HomePage(getDevices = { testDeviceOverview() })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    getDevices: () -> List<DeviceOverview> = { mutableListOf() },
) {
    var devices = getDevices().toMutableStateList()
    val refreshState = rememberPullToRefreshState()
    if (refreshState.isRefreshing) {
        LaunchedEffect(true) {
            devices = getDevices().toMutableStateList()
            refreshState.endRefresh()
        }
    }

    Box(Modifier.nestedScroll(refreshState.nestedScrollConnection)) {
        LazyColumn(
            modifier =
                modifier
                    .fillMaxSize(),
        ) {
            items(devices) { deviceOverview ->
                DeviceCard(
                    deviceOverview = deviceOverview,
                    onRemove = {
                        devices.remove(it)
                    },
                )
            }
        }

        PullToRefreshContainer(
            modifier = Modifier.align(Alignment.TopCenter),
            state = refreshState,
        )
    }
}

@Composable
fun DeviceCard(
    deviceOverview: DeviceOverview,
    onRemove: (DeviceOverview) -> Unit,
) {
    var expandedCard by rememberSaveable { mutableStateOf(false) }

    val rotationState by animateFloatAsState(
        targetValue = if (expandedCard) 180f else 0f,
        label = "Arrow down rotation",
    )

    val deviceState by remember { mutableStateOf(getDeviceState(deviceOverview)) }

    ElevatedCard(
        modifier =
            Modifier
                .animateContentSize()
                .fillMaxWidth()
                .padding(8.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(12.dp),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .clickable(
                            onClick = { expandedCard = !expandedCard },
                        ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    DeviceStateIcon(deviceState)
                }

                Column {
                    Text(
                        text = deviceOverview.device.name,
                        modifier =
                            Modifier.padding(8.dp),
                    )
                }

                Spacer(Modifier.weight(1f))

                Column {
                    Icon(
                        modifier = Modifier.rotate(rotationState),
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Drop-Down Arrow",
                    )
                }
            }
            if (expandedCard) {
                DeviceInfo(modifier = Modifier, deviceOverview.device)

                DeviceStatus(
                    modifier = Modifier,
                    deviceOverview = deviceOverview,
                    state = deviceState,
                )

                Box(modifier = Modifier.fillMaxWidth().padding(0.dp), contentAlignment = Alignment.BottomEnd) {
                    DeleteDeviceIconButton(
                        device = deviceOverview.device,
                        onRemove = {
                            expandedCard = false
                            onRemove(deviceOverview)
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun DeviceStateIcon(state: DeviceState) {
    when (state) {
        DeviceState.Active -> {
            Circle(MaterialTheme.colorScheme.success)
        }
        DeviceState.Scheduled -> {
            Circle(MaterialTheme.colorScheme.scheduled)
        }
        DeviceState.Inactive -> {
            Circle(MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun Circle(color: Color) {
    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
}

@Composable
fun DeviceInfo(
    modifier: Modifier = Modifier,
    device: Device,
) {
    Column {
        Text(
            modifier = modifier,
            text = "Effect: " + device.effect + " W",
        )
    }
}

@Composable
fun DeviceStatus(
    modifier: Modifier = Modifier,
    deviceOverview: DeviceOverview,
    state: DeviceState,
) {
    when (state) {
        DeviceState.Active -> {
            val taskEvent = deviceOverview.taskEvent
            if (taskEvent != null) {
                val event = taskEvent.event
                if (event != null) {
                    Text(
                        modifier = modifier,
                        text =
                            "This device ends at " +
                                event.start_time
                                    .plus(taskEvent.task.duration, ChronoUnit.MILLIS)
                                    .format(DATE_FORMATTER),
                    )
                }
            }
        }
        DeviceState.Scheduled -> {
            val taskEvent = deviceOverview.taskEvent
            if (taskEvent != null) {
                val event = taskEvent.event
                if (event != null) {
                    Text(
                        modifier = modifier,
                        text =
                            "This device starts in " +
                                event.start_time.format(DATE_FORMATTER),
                    )
                }
            }
        }
        DeviceState.Inactive -> {
            Text(
                modifier = modifier,
                text = "No events",
            )
        }
    }
}

@Composable
fun DeleteDeviceIconButton(
    device: Device,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var openConfirmDialog by remember { mutableStateOf(false) }

    if (openConfirmDialog) {
        AlertDialog(
            title = {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Remove " + device.name,
                    textAlign = TextAlign.Center,
                )
            },
            text = {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Are you sure that you want to remove " + device.name + "?",
                    textAlign = TextAlign.Center,
                )
            },
            confirmButton = {
                FilledButton(
                    onClick = {
                        openConfirmDialog = false
                        // TODO: Call API to remove a device
                        onRemove()
                    },
                    text = "Confirm",
                )
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        openConfirmDialog = false
                    },
                    text = "Cancel",
                )
            },
            onDismissRequest = { openConfirmDialog = false },
        )
    }

    IconButton(
        onClick = {
            openConfirmDialog = true
        },
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete device",
            tint = MaterialTheme.colorScheme.error,
        )
    }
}
