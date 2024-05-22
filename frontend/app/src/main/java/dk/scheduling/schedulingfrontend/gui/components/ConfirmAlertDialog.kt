package dk.scheduling.schedulingfrontend.gui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign

@Composable
fun ConfirmAlertDialog(
    openConfirmDialog: Boolean,
    setOpenConfirmDialog: (Boolean) -> Unit,
    title: String,
    text: String,
    confirmLabel: String = "Confirm",
    onConfirm: () -> Unit,
    dismissLabel: String = "Reject",
    onDismiss: () -> Unit = {},
) {
    if (openConfirmDialog) {
        AlertDialog(
            title = {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = title,
                    textAlign = TextAlign.Center,
                )
            },
            text = {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = text,
                    textAlign = TextAlign.Center,
                )
            },
            confirmButton = {
                FilledButton(
                    onClick = {
                        setOpenConfirmDialog(false)
                        onConfirm()
                    },
                    text = confirmLabel,
                )
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        setOpenConfirmDialog(false)
                        onDismiss()
                    },
                    text = dismissLabel,
                )
            },
            onDismissRequest = { setOpenConfirmDialog(false) },
        )
    }
}
