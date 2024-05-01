@file:OptIn(ExperimentalMaterial3Api::class)

package dk.scheduling.schedulingfrontend.components

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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dk.scheduling.schedulingfrontend.ui.theme.SchedulingFrontendTheme

@Composable
fun <T> StandardDropDownMenu(
    modifier: Modifier,
    options: Map<T, String>,
    label: String,
    selectedItem: T?,
    onSelect: (T) -> Unit,
) {
    val (expanded, setExpanded) = remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = setExpanded,
        modifier = modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            // The `menuAnchor` modifier must be passed to the text field for correctness.
            modifier =
                modifier
                    .fillMaxWidth()
                    .menuAnchor(),
            value = options[selectedItem] ?: "Unselected",
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { setExpanded(false) },
            modifier = modifier,
        ) {
            options.forEach { (key, value) ->
                DropdownMenuItem(
                    modifier = modifier,
                    text = {
                        Text(
                            value,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    onClick = {
                        onSelect(key)
                        setExpanded(false)
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
    val options = mapOf(1 to "Washer", 2 to "Dryer", 3 to "Toaster")
    val (selectedItem, setSelectedItem) = remember { mutableIntStateOf(1) }
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
                onSelect = setSelectedItem,
            )
        }
    }
}
