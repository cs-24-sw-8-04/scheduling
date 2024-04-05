package dk.scheduling.schedulingfrontend.device

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import dk.scheduling.schedulingfrontend.sharedcomponents.FilledButton
import dk.scheduling.schedulingfrontend.sharedcomponents.OutlinedButton
import dk.scheduling.schedulingfrontend.sharedcomponents.StandardTextField

@Composable
fun CreateDevicePage(
    modifier: Modifier = Modifier,
    navigateOnValidCreation: () -> Unit,
    navigateOnCancelCreation: () -> Unit,
) {
    var device by remember {
        mutableStateOf(Device(-1, "", null))
    }

    var isEffectSet by remember {
        mutableStateOf(true)
    }
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(all = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Spacer(modifier = Modifier.height(90.dp))
        Text(
            text = "Create a Device",
            fontSize = 7.em,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight(700),
        )
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(all = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        StandardTextField(
            modifier = modifier,
            "Device Name",
            device.name,
            onValueChange = { device = Device(-1, it, device.effect) },
        )

        Spacer(modifier = Modifier.height(20.dp))

        StandardTextField(
            modifier = modifier,
            "Effect (W)",
            if (device.effect != null) device.effect.toString() else "",
            onValueChange = {
                var effect: Int? = null
                if (it == "") {
                    isEffectSet = false
                } else {
                    effect = it.toInt()
                    isEffectSet = true
                }

                device = Device(-1, device.name, effect)
            },
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                ),
            isError = !isEffectSet,
        )

        Spacer(modifier = Modifier.height(30.dp))

        FilledButton(
            onClick = {
                if (createDevice(device)) {
                    navigateOnValidCreation()
                }
            },
            text = "Create Device",
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = {
                navigateOnCancelCreation()
            },
            text = "Cancel",
        )
    }
}

fun createDevice(device: Device): Boolean {
    // Send info to the server and if all goes well return true otherwise false
    return false
}

@Preview(showBackground = true, device = "spec:id=reference_phone,shape=Normal,width=411,height=891,unit=dp,dpi=420")
@Composable
fun CreateDevicePagePreview() {
    CreateDevicePage(navigateOnValidCreation = {}, navigateOnCancelCreation = {})
}
