package dk.scheduling.schedulingfrontend.gui.components
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import dk.scheduling.schedulingfrontend.gui.theme.SchedulingFrontendTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandardTimePickerDialog(
    closeDialog: () -> Unit,
    state: TimePickerState,
    isDialogOpen: Boolean,
) {
    if (isDialogOpen) {
        val (showingPicker, setShowingPicker) = remember { mutableStateOf(true) }
        BasicAlertDialog(
            // Dismiss the dialog when the user clicks outside the dialog or on the back
            // button. If you want to disable that functionality, simply use an empty
            // onDismissRequest.
            onDismissRequest = closeDialog,
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
                    if (showingPicker) {
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
                        ChangeInput({ setShowingPicker(!showingPicker) }, showingPicker)
                        Button(onClick = closeDialog) {
                            Text(text = "Close")
                        }
                        Button(onClick = closeDialog) {
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
    IconButton(onClick = changeInputType) {
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
        val (isDialogOpen, setOpenDialog) = remember { mutableStateOf(true) }
        val state = rememberTimePickerState()

        StandardTimePickerDialog(
            closeDialog = { setOpenDialog(false) },
            state = state,
            isDialogOpen = isDialogOpen,
        )
        val dateMsg = "Time: ${state.hour}:${state.minute}"
        Text(text = dateMsg)
    }
}
