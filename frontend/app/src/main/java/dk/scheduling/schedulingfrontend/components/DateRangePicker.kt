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
import dk.scheduling.schedulingfrontend.ui.theme.SchedulingFrontendTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandardDateRangePicker(
    closeDialog: () -> Unit,
    passingDate: (DateRange) -> Unit,
    openDialog: Boolean,
) {
    if (openDialog) {
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
                        IconButton(onClick = { closeDialog() }) {
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

class DateRange {
    constructor(startTime: Long, endTime: Long) {
        rangeStart = startTime
        rangeEnd = endTime
    }
    constructor() {
        rangeStart = null
        rangeEnd = null
    }

    private val rangeStart: Long?
    private val rangeEnd: Long?

    fun isValidRange(): Boolean {
        return rangeStart != null && rangeEnd != null && rangeStart <= rangeEnd
    }

    private fun convertMillisToDate(millis: Long): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.GERMANY)
        return formatter.format(Date(millis))
    }

    fun getStartDate(): String {
        return if (isValidRange()) convertMillisToDate(rangeStart!!) else "No start date"
    }

    fun getEndDate(): String {
        return if (isValidRange()) convertMillisToDate(rangeEnd!!) else "No end date"
    }

    fun print(): String {
        return getStartDate() + ":" + getEndDate()
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
            openDialog = openDialog.value,
        )
        val dateRange = "${datePickerState.value.getStartDate()}:${datePickerState.value.getEndDate()}"
        val dateMsg = if (datePickerState.value.isValidRange()) "no interval selected." else dateRange
        Text(text = "Time: $dateMsg")
    }
}
