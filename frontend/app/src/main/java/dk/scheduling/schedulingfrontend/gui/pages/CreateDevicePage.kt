package dk.scheduling.schedulingfrontend.gui.pages

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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import dk.scheduling.schedulingfrontend.datasources.api.protocol.Device
import dk.scheduling.schedulingfrontend.gui.components.FilledButton
import dk.scheduling.schedulingfrontend.gui.components.OutlinedButton
import dk.scheduling.schedulingfrontend.gui.components.StandardTextField
import dk.scheduling.schedulingfrontend.repositories.device.IDeviceRepository
import kotlinx.coroutines.launch
import testdata.DummyDeviceRepository

@Composable
fun CreateDevicePage(
    modifier: Modifier = Modifier,
    deviceRepository: IDeviceRepository,
    navigateOnValidCreation: () -> Unit,
    navigateOnCancelCreation: () -> Unit,
) {
    var device by remember {
        mutableStateOf(Device(-1, "", 0.0))
    }

    var isEffectSet by remember {
        mutableStateOf(true)
    }

    var errorStatus: String? by remember {
        mutableStateOf(null)
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
            label = "Device Name",
            value = device.name,
            onValueChange = { device = Device(device.id, it, device.effect) },
        )

        Spacer(modifier = Modifier.height(20.dp))

        StandardTextField(
            label = "Effect (W)",
            value = device.effect.toString(),
            onValueChange = {
                val effect = it.toDoubleOrNull()
                isEffectSet = effect != null

                device = Device(device.id, device.name, effect ?: 0.0)
            },
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                ),
            isError = !isEffectSet,
        )

        Spacer(modifier = Modifier.height(30.dp))

        errorStatus?.let {
            Text(
                text = it,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error,
            )
            Spacer(modifier = Modifier.height(30.dp))
        }

        val coroutineScope = rememberCoroutineScope()

        FilledButton(
            onClick = {
                try {
                    coroutineScope.launch {
                        deviceRepository.createDevice(device.name, device.effect)
                        errorStatus = null
                        navigateOnValidCreation()
                    }
                } catch (e: Throwable) {
                    errorStatus = "A device could not be created"
                }
            },
            text = "Create Device",
            enabled = device.name.isNotBlank() && device.effect > 0,
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

@Preview(showBackground = true, device = "spec:id=reference_phone,shape=Normal,width=411,height=891,unit=dp,dpi=420")
@Composable
fun CreateDevicePagePreview() {
    CreateDevicePage(deviceRepository = DummyDeviceRepository(0), navigateOnValidCreation = {}, navigateOnCancelCreation = {})
}
