@file:OptIn(
    ExperimentalMaterial3Api::class,
)

package dk.scheduling.schedulingfrontend

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
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dk.scheduling.schedulingfrontend.sharedcomponents.FilledButton
import dk.scheduling.schedulingfrontend.sharedcomponents.StandardDateRangePicker
import dk.scheduling.schedulingfrontend.sharedcomponents.StandardDropDownMenu
import dk.scheduling.schedulingfrontend.sharedcomponents.StandardTimePickerDialog
import dk.scheduling.schedulingfrontend.sharedcomponents.Title
import dk.scheduling.schedulingfrontend.ui.theme.SchedulingFrontendTheme

@Composable
fun CreateTaskPage(
    modifier: Modifier = Modifier,
    handleSubmission: () -> Unit,
    handleCancellation: () -> Unit,
) {
    val options = listOf("Washer", "Dryer", "Toaster")
    var selectedItem by remember { mutableStateOf(options[0]) }
    Title(titleText = "Create Task", topMargin = 0.dp)

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Spacer(modifier = Modifier.height(100.dp))
        StandardDropDownMenu(
            modifier = Modifier,
            options = options,
            label = "Devices",
            selectedItem = selectedItem,
            onSelect = { selectedItem = it },
        )

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
        val datePickerState = remember { mutableStateOf(Long.MIN_VALUE..Long.MIN_VALUE) }

        StandardDateRangePicker(
            closeDialog = { dateRangeDialog.value = false },
            passingDate = { datePickerState.value = it },
            openDialog = dateRangeDialog.value,
        )
        val dateRange = "${datePickerState.value.first}:${datePickerState.value.last}"
        val dateMsg: String = if (datePickerState.value.first == Long.MIN_VALUE) "no interval selected." else dateRange

        ClickableCard({ dateRangeDialog.value = true }, "Date interval: $dateMsg")

        Spacer(modifier = Modifier.height(30.dp))

        // start time
        val startTimeDialog = remember { mutableStateOf(false) }
        val startHour = remember { mutableIntStateOf(-1) }
        val startMinute = remember { mutableIntStateOf(-1) }

        StandardTimePickerDialog(
            closeDialog = { startTimeDialog.value = false },
            passValue = {
                startHour.intValue = it.hour
                startMinute.intValue = it.minute
            },
            openDialog = startTimeDialog.value,
        )
        ClickableCard({ startTimeDialog.value = true }, "Start time: ${formatTime(startHour.intValue, startMinute.intValue)}")

        // end time
        Spacer(modifier = Modifier.height(30.dp))

        val endTimeDialog = remember { mutableStateOf(false) }
        val endHour = remember { mutableIntStateOf(-1) }
        val endMinute = remember { mutableIntStateOf(-1) }

        StandardTimePickerDialog(
            closeDialog = { endTimeDialog.value = false },
            passValue = {
                endHour.intValue = it.hour
                endMinute.intValue = it.minute
            },
            openDialog = endTimeDialog.value,
        )
        ClickableCard({ endTimeDialog.value = true }, "End time: ${formatTime(startHour.intValue, startMinute.intValue)}")

        Spacer(modifier = Modifier.height(30.dp))

        FilledButton(
            onClick = { handleSubmission() },
            text = "Create task",
        )

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
    return if (hour == -1 || minute == -1) {
        "Unselected"
    } else {
        val hourStr = if (hour < 10) "0$hour" else hour
        val minuteStr = if (minute < 10) "0$minute" else minute
        "$hourStr:$minuteStr"
    }
}

fun numbersOnly(input: String): Boolean {
    return input.all { char -> char.isDigit() }
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
