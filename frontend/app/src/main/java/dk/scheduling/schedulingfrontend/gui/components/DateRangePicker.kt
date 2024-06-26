package dk.scheduling.schedulingfrontend.gui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties
import dk.scheduling.schedulingfrontend.gui.theme.SchedulingFrontendTheme
import dk.scheduling.schedulingfrontend.model.Status
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandardDateRangePicker(
    closeDialog: () -> Unit,
    passingDate: (DateRange) -> Unit,
    isDialogOpen: Boolean,
) {
    if (isDialogOpen) {
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
                val state =
                    rememberDateRangePickerState(
                        selectableDates =
                            object : SelectableDates {
                                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                                    return utcTimeMillis >= System.currentTimeMillis() - 86400000
                                }
                            },
                    )
                Column(
                    verticalArrangement = Arrangement.Top,
                ) {
                    DialogActions(closeDialog, state, passingDate)
                    DateRangePicker(
                        state = state,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogActions(
    closeDialog: () -> Unit,
    state: DateRangePickerState,
    passingDate: (DateRange) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = closeDialog) {
            Icon(Icons.Filled.Close, contentDescription = "Close")
        }
        TextButton(
            onClick = {
                val range =
                    DateRange(state.selectedStartDateMillis!!, state.selectedEndDateMillis!!)
                passingDate(range)
                closeDialog()
            },
            enabled = state.selectedEndDateMillis != null,
        ) {
            Text(text = "Save")
        }
    }
}

data class DateRange(val startTime: Long?, val endTime: Long?) {
    private fun millisToLocalDateTime(millis: Long): LocalDateTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault()).with(LocalTime.MIDNIGHT)

    fun rangeStart(): LocalDateTime? = startTime?.let { millisToLocalDateTime(it) }

    fun rangeEnd(): LocalDateTime? = endTime?.let { millisToLocalDateTime(it) }

    fun isInitialized(): Boolean = !(rangeStart() == null && rangeEnd() == null)

    fun status(): Status {
        return if (!isInitialized()) {
            Status(false, "No interval selected")
        } else if (rangeStart() == null) {
            Status(false, "No start date")
        } else if (rangeEnd() == null) {
            Status(false, "No end date")
        } else if (!(rangeStart()!!.isBefore(rangeEnd()) || rangeStart()!!.isEqual(rangeEnd()))) {
            Status(false, "Start date must be before end date")
        } else {
            Status(true, "${rangeStart()!!.format(DATE_FORMAT)} to ${rangeEnd()!!.format(DATE_FORMAT)}")
        }
    }
}

@Preview(showBackground = true, device = "spec:id=reference_phone,shape=Normal,width=411,height=891,unit=dp,dpi=420")
@Composable
fun PickerPreviewLightMode() {
    SchedulingFrontendTheme(darkTheme = false, dynamicColor = false) {
        val openDialog = remember { mutableStateOf(true) }
        val (datePickerState, setDatePickerState) = remember { mutableStateOf(DateRange(Long.MIN_VALUE, Long.MIN_VALUE)) }

        StandardDateRangePicker(
            closeDialog = { openDialog.value = false },
            passingDate = setDatePickerState,
            isDialogOpen = openDialog.value,
        )
        Text(text = "Time: ${datePickerState.status().msg}")
    }
}
