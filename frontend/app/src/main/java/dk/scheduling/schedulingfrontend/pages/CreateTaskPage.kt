@file:OptIn(
    ExperimentalMaterial3Api::class,
)

package dk.scheduling.schedulingfrontend.pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dk.scheduling.schedulingfrontend.components.DateRange
import dk.scheduling.schedulingfrontend.components.FilledButton
import dk.scheduling.schedulingfrontend.components.StandardDateRangePicker
import dk.scheduling.schedulingfrontend.components.StandardTimePickerDialog
import dk.scheduling.schedulingfrontend.components.Title
import dk.scheduling.schedulingfrontend.sharedcomponents.StandardDropDownMenu
import dk.scheduling.schedulingfrontend.ui.theme.SchedulingFrontendTheme

@Composable
fun CreateTaskPage(
    modifier: Modifier = Modifier,
    handleSubmission: () -> Unit,
    handleCancellation: () -> Unit,
) {
    Title(titleText = "Create Task", topMargin = 0.dp)

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // Dropdown box of devices
        Spacer(modifier = Modifier.height(100.dp))

        val options = listOf("Washer", "Dryer", "Toaster")
        var selectedItem by remember { mutableStateOf(options[0]) }
        StandardDropDownMenu(
            modifier = Modifier,
            options = options,
            label = "Devices",
            selectedItem = selectedItem,
            onSelect = { selectedItem = it },
        )

        // Duration input field
        Spacer(modifier = Modifier.height(20.dp))

        var duration: String by rememberSaveable { mutableStateOf("") }
        var numbersOnly: Boolean by rememberSaveable { mutableStateOf(true) }

        OutlinedTextField(
            modifier = modifier.fillMaxWidth(),
            value = duration,
            onValueChange = {
                duration = it
                numbersOnly = numbersOnly(duration)
            },
            label = { Text("Duration (m)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = !numbersOnly,
        )

        Spacer(modifier = Modifier.height(20.dp))

        // start date & end date
        val dateRangeDialog = remember { mutableStateOf(false) }
        val datePickerValue = remember { mutableStateOf(DateRange()) }

        StandardDateRangePicker(
            closeDialog = { dateRangeDialog.value = false },
            passingDate = { datePickerValue.value = it },
            openDialog = dateRangeDialog.value,
        )

        val dateMsg: String = if (datePickerValue.value.isValidRange()) "no interval selected." else datePickerValue.value.print()

        ClickableCard({ dateRangeDialog.value = true }, "Date interval: $dateMsg")

        Spacer(modifier = Modifier.height(30.dp))

        // start time
        val startTimeDialog = remember { mutableStateOf(false) }
        val startTimeState = rememberTimePickerState()

        StandardTimePickerDialog(
            closeDialog = { startTimeDialog.value = false },
            state = startTimeState,
            openDialog = startTimeDialog.value,
        )
        ClickableCard({ startTimeDialog.value = true }, "Start time: ${formatTime(startTimeState.hour, startTimeState.minute)}")

        // end time
        Spacer(modifier = Modifier.height(30.dp))

        val endTimeDialog = remember { mutableStateOf(false) }
        val endTimeState = rememberTimePickerState()

        StandardTimePickerDialog(
            closeDialog = { endTimeDialog.value = false },
            state = endTimeState,
            openDialog = endTimeDialog.value,
        )
        ClickableCard({ endTimeDialog.value = true }, "End time: ${formatTime(endTimeState.hour, endTimeState.minute)}")

        // Submit or Cancel
        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = { handleSubmission() },
            enabled = isValidInput(duration, datePickerValue.value, startTimeState, endTimeState),
            modifier = modifier.fillMaxWidth(),
        ) {
            Text(text = "Create task")
        }

        FilledButton(
            onClick = {
                handleCancellation()
            },
            text = "Cancel",
        )
    }
}

@Composable
fun ClickableCard(
    onClick: () -> Unit,
    text: String,
) {
    Card(
        onClick = { onClick() },
        modifier = Modifier.fillMaxWidth().size(width = 100.dp, height = 50.dp),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
    ) {
        Box(Modifier.fillMaxSize()) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = text,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

fun formatTime(
    hour: Int,
    minute: Int,
): String {
    val hourStr = if (hour < 10) "0$hour" else hour
    val minuteStr = if (minute < 10) "0$minute" else minute
    return "$hourStr:$minuteStr"
}

fun numbersOnly(input: String): Boolean {
    return input.all { char -> char.isDigit() }
}

fun isValidDuration(input: String): Boolean {
    return numbersOnly(input) && input.isNotEmpty() && input.isNotBlank() && !input.all { char -> char == '0' }
}

fun isValidInput(
    duration: String,
    dateRange: DateRange,
    startTime: TimePickerState,
    endTime: TimePickerState,
): Boolean {
    // Max line length is violated if not split into two expressions.
    val startTimeIsBeforeEndTime = startTime.hour <= endTime.hour && startTime.minute < endTime.minute
    val startIsBeforeEnd = dateRange.getStartDate() != dateRange.getEndDate() || startTimeIsBeforeEndTime
    return isValidDuration(
        duration,
    ) && dateRange.isValidRange() && startIsBeforeEnd
}

@Preview(showBackground = true, device = "spec:id=reference_phone,shape=Normal,width=411,height=891,unit=dp,dpi=420")
@Composable
fun CreateTaskPagePreviewLightMode() {
    SchedulingFrontendTheme(darkTheme = false, dynamicColor = false) {
        CreateTaskPage(Modifier, handleSubmission = {}, handleCancellation = {})
    }
}

@Preview(showBackground = true, device = "spec:id=reference_phone,shape=Normal,width=411,height=891,unit=dp,dpi=420")
@Composable
fun CreateTaskPagePreviewDarkMode() {
    SchedulingFrontendTheme(darkTheme = true, dynamicColor = false) {
        CreateTaskPage(Modifier, handleSubmission = {}, handleCancellation = {})
    }
}
