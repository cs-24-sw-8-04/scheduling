package dk.scheduling.schedulingfrontend

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dk.scheduling.schedulingfrontend.device.Device
import dk.scheduling.schedulingfrontend.device.DeviceOverview
import dk.scheduling.schedulingfrontend.device.DeviceState
import dk.scheduling.schedulingfrontend.device.getDeviceState
import dk.scheduling.schedulingfrontend.sharedcomponents.DATE_FORMATTER
import dk.scheduling.schedulingfrontend.sharedcomponents.FilledButton
import dk.scheduling.schedulingfrontend.sharedcomponents.OutlinedButton
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

@Preview(showBackground = true, device = "spec:id=reference_phone,shape=Normal,width=411,height=891,unit=dp,dpi=420")
@Composable
fun HomePagePreviewDarkMode() {
    SchedulingFrontendTheme(darkTheme = true, dynamicColor = false) {
        HomePage(getDevices = { testDeviceOverview() })
    }
}

@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    getDevices: () -> List<DeviceOverview> = { mutableListOf() },
) {
    val devices = remember { getDevices().toMutableStateList() }

    LazyColumn(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
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
}

@Composable
fun DeviceCard(
    deviceOverview: DeviceOverview,
    onRemove: (DeviceOverview) -> Unit,
) {
    var expandedCard by remember { mutableStateOf(false) }

    val rotationState by animateFloatAsState(
        targetValue = if (expandedCard) 180f else 0f,
        label = "Arrow down rotation",
    )

    val deviceState by remember { mutableStateOf(getDeviceState(deviceOverview)) }

    Card(
        shape = RoundedCornerShape(5.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier =
            Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth(),
    ) {
        val modifier = Modifier.padding(horizontal = 10.dp)

        Row(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp)
                    .clickable(
                        onClick = { expandedCard = !expandedCard },
                    ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier =
                    Modifier.fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DeviceStateIcon(deviceState)

                Text(
                    text = deviceOverview.device.name,
                    modifier = Modifier.padding(8.dp),
                )
            }

            Icon(
                modifier = Modifier.rotate(rotationState),
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Drop-Down Arrow",
            )
        }

        if (expandedCard) {
            HorizontalDivider(
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(modifier = Modifier.height(7.dp))

            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(0.8f),
                ) {
                    DeviceInfo(device = deviceOverview.device)

                    Spacer(modifier = Modifier.height(7.dp))

                    DeviceStatus(
                        deviceOverview = deviceOverview,
                        state = deviceState,
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(0.5f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    DeleteDeviceIconButton(
                        device = deviceOverview.device,
                        onRemove = {
                            expandedCard = false
                            onRemove(deviceOverview)
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(7.dp))
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
            val event = deviceOverview.event
            if (event != null) {
                Text(
                    modifier = modifier,
                    text = "This device ends at " + event.startTime.plus(event.duration, ChronoUnit.MILLIS).format(DATE_FORMATTER),
                )
            }
        }
        DeviceState.Scheduled -> {
            val event = deviceOverview.event
            if (event != null) {
                Text(
                    modifier = modifier,
                    text =
                        "This device starts in " +
                            event.startTime.format(
                                DATE_FORMATTER,
                            ),
                )
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
