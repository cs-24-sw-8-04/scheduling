package dk.scheduling.schedulingfrontend.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dk.scheduling.schedulingfrontend.ui.theme.SchedulingFrontendTheme
import kotlinx.coroutines.delay

@Composable
fun Loading(
    modifier: Modifier =
        Modifier
            .fillMaxWidth()
            .padding(10.dp),
    isLoading: Boolean,
    setIsLoading: (Boolean) -> Unit,
    onLoading: suspend () -> Unit,
    whenLoaded: @Composable () -> Unit,
) {
    LaunchedEffect(isLoading) {
        onLoading()
        setIsLoading(false)
    }

    if (isLoading) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LinearProgressIndicator(
                modifier = modifier,
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    } else {
        whenLoaded()
    }
}

@Preview(showBackground = true, device = "spec:id=reference_phone,shape=Normal,width=411,height=891,unit=dp,dpi=420")
@Composable
fun LoadingPreviewLightMode() {
    SchedulingFrontendTheme(darkTheme = false, dynamicColor = false) {
        val (isLoading, setIsLoading) = remember { mutableStateOf(true) }

        Loading(
            isLoading = isLoading,
            setIsLoading = setIsLoading,
            onLoading = {
                delay(5L * 1000L)
            },
        ) {
            Text(text = "Loaded")

            Text(text = "value of isloading = $isLoading")

            FilledButton(onClick = { setIsLoading(true) }, text = "Load")
        }
    }
}
