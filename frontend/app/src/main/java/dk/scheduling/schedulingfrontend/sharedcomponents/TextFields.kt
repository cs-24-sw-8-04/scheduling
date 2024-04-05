package dk.scheduling.schedulingfrontend.sharedcomponents

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TextLabel(label: String) {
    Text(
        label,
        style = TextStyle(fontSize = 15.sp),
    )
}

@Composable
fun StandardTextField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    isError: Boolean = false,
) {
    Column {
        TextLabel(label)
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier =
            modifier
                .fillMaxWidth()
                .border(
                    color = if (isError) MaterialTheme.colorScheme.error else Color.Black,
                    width = 1.dp,
                ),
            singleLine = true,
            keyboardOptions = keyboardOptions,
        )
    }
}