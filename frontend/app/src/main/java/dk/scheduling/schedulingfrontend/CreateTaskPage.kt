@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3Api::class,
)

package dk.scheduling.schedulingfrontend

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import dk.scheduling.schedulingfrontend.sharedcomponents.FilledButton
import dk.scheduling.schedulingfrontend.sharedcomponents.StandardDropDownMenu
import dk.scheduling.schedulingfrontend.sharedcomponents.Title
import dk.scheduling.schedulingfrontend.ui.theme.SchedulingFrontendTheme

@Composable
fun CreateTaskPage(
    modifier: Modifier = Modifier,
    handleSubmission: () -> Unit,
) {
    var isLoginFailed by remember {
        mutableStateOf(false)
    }
    val options = listOf("Washer", "Dryer", "Toaster")
    var selectedItem by remember { mutableStateOf(options[0]) }
    Title(titleText = "Create Task")

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(50.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
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

        // start date
        
        // start time

        // end date

        // end time

        Spacer(modifier = Modifier.height(30.dp))

        FilledButton(
            onClick = { handleSubmission() },
            text = "Create task",
        )

        FilledButton(
            onClick = {
                if (true) {
                    handleSubmission()
                } else {
                    isLoginFailed = true
                }
            },
            text = "Cancel",
        )
    }
}

fun numbersOnly(input: String): Boolean {
    return input.all { char -> char.isDigit() }
}

@Preview(showBackground = true, device = "spec:id=reference_phone,shape=Normal,width=411,height=891,unit=dp,dpi=420")
@Composable
fun CreateTaskPagePreviewLightMode() {
    SchedulingFrontendTheme(darkTheme = false, dynamicColor = false) {
        CreateTaskPage(Modifier, handleSubmission = {})
    }
}

@Preview(showBackground = true, device = "spec:id=reference_phone,shape=Normal,width=411,height=891,unit=dp,dpi=420")
@Composable
fun CreateTaskPagePreviewDarkMode() {
    SchedulingFrontendTheme(darkTheme = true, dynamicColor = false) {
        CreateTaskPage(Modifier, handleSubmission = {})
    }
}
