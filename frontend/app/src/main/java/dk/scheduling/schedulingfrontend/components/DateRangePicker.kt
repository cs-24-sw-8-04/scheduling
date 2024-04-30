package dk.scheduling.schedulingfrontend.components

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
import dk.scheduling.schedulingfrontend.model.Status
import dk.scheduling.schedulingfrontend.ui.theme.SchedulingFrontendTheme
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandardDateRangePicker(
    closeDialog: () -> Unit,
    passingDate: (DateRange) -> Unit,
    dialogState: Boolean,
) {
    if (dialogState) {
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
                                val range = DateRange(state.selectedStartDateMillis!!, state.selectedEndDateMillis!!)
                                passingDate(range)
                                closeDialog()
                            },
                            enabled = state.selectedEndDateMillis != null,
                        ) {
                            Text(text = "Save")
                        }
                    }
                    DateRangePicker(
                        state = state,
                    )
                }
            }
        }
    }
}

data class DateRange(val startTime: Long?, val endTime: Long?) {
    private fun millisToLocalDateTime(millis: Long): LocalDateTime {
        val instant = Instant.ofEpochMilli(millis)
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    }

    fun rangeStart(): LocalDateTime? {
        return startTime?.let { millisToLocalDateTime(it) }
    }

    fun rangeEnd(): LocalDateTime? {
        return endTime?.let { millisToLocalDateTime(it) }
    }

    fun isInitialized(): Boolean {
        return !(rangeStart() == null && rangeEnd() == null)
    }

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
            Status(true, "${rangeStart()!!.format(DATE_FORMAT)}:${rangeEnd()!!.format(DATE_FORMAT)}")
        }
    }
}

@Preview(showBackground = true, device = "spec:id=reference_phone,shape=Normal,width=411,height=891,unit=dp,dpi=420")
@Composable
fun PickerPreviewLightMode() {
    SchedulingFrontendTheme(darkTheme = false, dynamicColor = false) {
        val openDialog = remember { mutableStateOf(true) }
        val datePickerState = remember { mutableStateOf(DateRange(Long.MIN_VALUE, Long.MIN_VALUE)) }

        StandardDateRangePicker(
            closeDialog = { openDialog.value = false },
            passingDate = { datePickerState.value = it },
            dialogState = openDialog.value,
        )
        Text(text = "Time: ${datePickerState.value.status().msg}")
    }
}
