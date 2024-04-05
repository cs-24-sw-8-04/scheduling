package dk.scheduling.schedulingfrontend.sharedcomponents

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@Composable
fun FilledButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    text: String,
    ) {
    Column {
        Button(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
        ) {
            Text(text = text)
        }
    }
}

@Composable
fun OutlinedButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    text: String,
) {
    Column {
        androidx.compose.material3.OutlinedButton(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
        ) {
            Text(text = text)
        }
    }
}

