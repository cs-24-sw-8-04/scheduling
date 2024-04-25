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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import dk.scheduling.schedulingfrontend.model.Duration
import dk.scheduling.schedulingfrontend.model.TaskForm
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
                .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // Dropdown box of devices
        Spacer(modifier = Modifier.height(100.dp))

        val (task, taskSetter) =
            remember {
                mutableStateOf(
                    TaskForm(
                        0,
                        Duration(""),
                        DateRange(null, null),
                        TimePickerState(0, 0, true),
                        TimePickerState(0, 0, true),
                    ),
                )
            }

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
        OutlinedTextField(
            modifier = modifier.fillMaxWidth(),
            value = task.duration.value,
            onValueChange = {
                taskSetter(task.copy(duration = Duration(it)))
            },
            label = { Text("Duration (min.)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = task.duration.initializedAndInvalid(),
        )

        // start date & end date
        val dateRangeDialog = remember { mutableStateOf(false) }

        StandardDateRangePicker(
            closeDialog = { dateRangeDialog.value = false },
            passingDate = {
                taskSetter(task.copy(dateRange = it.copy()))
            },
            openDialog = dateRangeDialog.value,
        )

        ClickableCard(
            { dateRangeDialog.value = true },
            "Date interval: ${task.dateRange.status().msg}",
            task.dateRange.isInitialized() && !task.dateRange.status().isValid,
        )

        // start time
        val startTimeDialog = remember { mutableStateOf(false) }

        StandardTimePickerDialog(
            closeDialog = { startTimeDialog.value = false },
            state = task.startTime,
            openDialog = startTimeDialog.value,
        )
        ClickableCard({ startTimeDialog.value = true }, "Start time: ${task.printStartTime()}")

        // end time
        val endTimeDialog = remember { mutableStateOf(false) }

        StandardTimePickerDialog(
            closeDialog = { endTimeDialog.value = false },
            state = task.endTime,
            openDialog = endTimeDialog.value,
        )
        ClickableCard({ endTimeDialog.value = true }, "End time: ${task.printEndTime()}")

        if (!task.status().isValid) {
            Text(
                text = task.status().msg,
                color = MaterialTheme.colorScheme.error,
            )
        }

        // Submit or Cancel
        Button(
            onClick = handleSubmission,
            enabled = task.status().isValid,
            modifier = modifier.fillMaxWidth(),
        ) {
            Text(text = "Create task")
        }

        FilledButton(
            onClick = handleCancellation,
            text = "Cancel",
        )
    }
}

@Composable
fun ClickableCard(
    onClick: () -> Unit,
    text: String,
    isError: Boolean = false,
) {
    Card(
        onClick = onClick,
        modifier =
            Modifier
                .fillMaxWidth()
                .size(width = 100.dp, height = 50.dp),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
    ) {
        Box(Modifier.fillMaxSize()) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = text,
                maxLines = 1,
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
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
