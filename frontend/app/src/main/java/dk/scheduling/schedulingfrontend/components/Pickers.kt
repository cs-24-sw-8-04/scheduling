package dk.scheduling.schedulingfrontend.sharedcomponents
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dk.scheduling.schedulingfrontend.ui.theme.SchedulingFrontendTheme
import kotlinx.coroutines.launch

@ExperimentalMaterial3Api
@Composable
fun Dia(
    closeDialog: () -> Unit,
    assingDate: (DatePickerState) -> Unit,
    openDialog: Boolean,
) {
// Decoupled snackbar host state from scaffold state for demo purposes.
    val snackState = remember { SnackbarHostState() }
    val snackScope = rememberCoroutineScope()
    SnackbarHost(hostState = snackState, Modifier)
// TODO demo how to read the selected date from the state.
    if (openDialog) {
        val datePickerState = rememberDatePickerState()
        val confirmEnabled =
            remember {
                derivedStateOf { datePickerState.selectedDateMillis != null }
            }
        DatePickerDialog(
            onDismissRequest = {
                // Dismiss the dialog when the user clicks outside the dialog or on the back
                // button. If you want to disable that functionality, simply use an empty
                // onDismissRequest. ,
                closeDialog()
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        assingDate(datePickerState)
                        closeDialog()
                        snackScope.launch {
                            snackState.showSnackbar(
                                "Selected date timestamp: ${datePickerState.selectedDateMillis}",
                            )
                        }
                    },
                    enabled = confirmEnabled.value,
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        closeDialog()
                    },
                ) {
                    Text("Cancel")
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, device = "spec:id=reference_phone,shape=Normal,width=411,height=891,unit=dp,dpi=420")
@Composable
fun TimePickerDialogPreviewLightMode() {
    SchedulingFrontendTheme(darkTheme = false, dynamicColor = false) {
        val openDialog = remember { mutableStateOf(true) }
        val datePickerState = remember { mutableLongStateOf(Long.MIN_VALUE) }

        Dia(
            closeDialog = { openDialog.value = false },
            assingDate = { datePickerState.longValue = it.selectedDateMillis!! },
            openDialog = openDialog.value,
        )
        val dateMsg: String = if (datePickerState.longValue == Long.MIN_VALUE) "no value selected." else "${datePickerState.longValue}"
        Text(text = "Time: $dateMsg")
    }
}
