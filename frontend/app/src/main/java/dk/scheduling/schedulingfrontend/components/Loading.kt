package dk.scheduling.schedulingfrontend.components

import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Loading(
    isLoading: Boolean,
    setIsLoading: (Boolean) -> Unit,
    onLoading: () -> Unit,
    whenLoaded: @Composable () -> Unit,
) {
    LaunchedEffect(isLoading) {
        onLoading()
        setIsLoading(false)
    }

    if (isLoading) {
        LinearProgressIndicator(
            modifier = Modifier.width(64.dp),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    } else {
        whenLoaded()
    }
}
