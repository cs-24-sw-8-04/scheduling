@file:OptIn(ExperimentalMaterial3Api::class)

package dk.scheduling.schedulingfrontend.sharedcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dk.scheduling.schedulingfrontend.ui.theme.SchedulingFrontendTheme

@Composable
fun StandardDropDownMenu(
    modifier: Modifier,
    options: List<String>,
    label: String,
    selectedItem: String,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            // The `menuAnchor` modifier must be passed to the text field for correctness.
            modifier =
                modifier
                    .fillMaxWidth()
                    .menuAnchor(),
            value = selectedItem,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = modifier,
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    modifier = modifier,
                    text = { Text(option, style = MaterialTheme.typography.bodyLarge) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:id=reference_phone,shape=Normal,width=411,height=891,unit=dp,dpi=420")
@Composable
fun StandardDropDownMenuPreviewLightMode() {
    val options = listOf("Washer", "Dryer", "Toaster")
    var selectedItem by remember { mutableStateOf(options[0]) }
    SchedulingFrontendTheme(darkTheme = false, dynamicColor = false) {
        Column(
            modifier =
                Modifier
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
        }
    }
}
