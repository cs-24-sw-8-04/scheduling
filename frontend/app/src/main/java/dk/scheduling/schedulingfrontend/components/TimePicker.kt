package dk.scheduling.schedulingfrontend.sharedcomponents
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import dk.scheduling.schedulingfrontend.ui.theme.SchedulingFrontendTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandardTimePickerDialog(
    closeDialog: () -> Unit,
    passValue: (TimePickerState) -> Unit,
    openDialog: Boolean,
) {
    // val state = rememberTimePickerState()
    // val showingPicker = remember { mutableStateOf(true) }
    if (openDialog) {
        val showingPicker = remember { mutableStateOf(true) }
        BasicAlertDialog(
            onDismissRequest = {
                // Dismiss the dialog when the user clicks outside the dialog or on the back
                // button. If you want to disable that functionality, simply use an empty
                // onDismissRequest.
                closeDialog()
            },
            properties =
                DialogProperties(
                    usePlatformDefaultWidth = false,
                ),
        ) {
            Surface(
                modifier =
                    Modifier
                        .wrapContentWidth()
                        .wrapContentHeight(),
                shape = MaterialTheme.shapes.large,
            ) {
                Column(
                    modifier = Modifier.padding(2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    val state = rememberTimePickerState()
                    if (showingPicker.value) {
                        TimePicker(
                            state = state,
                            modifier = Modifier.padding(8.dp),
                        )
                    } else {
                        TimeInput(state = state, modifier = Modifier.padding(8.dp))
                    }
                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        ChangeInput({ showingPicker.value = !showingPicker.value }, showingPicker.value)
                        Button(onClick = { closeDialog() }) {
                            Text(text = "Close")
                        }
                        Button(onClick = {
                            passValue(state)
                            closeDialog()
                        }) {
                            Text(text = "Confirm")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChangeInput(
    changeInputType: () -> Unit,
    showingPicker: Boolean,
) {
    IconButton(onClick = { changeInputType() }) {
        val icon =
            if (showingPicker) {
                Icons.Outlined.Keyboard
            } else {
                Icons.Outlined.Schedule
            }
        Icon(
            icon,
            contentDescription =
                if (showingPicker) {
                    "Switch to Text Input"
                } else {
                    "Switch to Touch Input"
                },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, device = "spec:id=reference_phone,shape=Normal,width=411,height=891,unit=dp,dpi=420")
@Composable
fun StandardTimePickerDialogPreviewLightMode() {
    SchedulingFrontendTheme(darkTheme = false, dynamicColor = false) {
        val openDialog = remember { mutableStateOf(true) }
        val hour = remember { mutableIntStateOf(1) }
        val minute = remember { mutableIntStateOf(1) }

        StandardTimePickerDialog(
            closeDialog = { openDialog.value = false },
            passValue = {
                hour.intValue = it.hour
                minute.intValue = it.minute
            },
            openDialog = openDialog.value,
        )
        val dateMsg: String = "Time: $hour:$minute"
        Text(text = dateMsg)
    }
}
