package dk.scheduling.schedulingfrontend.components

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun PermissionDialog(permission: Permission) {
}

sealed class Permission(
    val permission: String,
    val title: String,
    val description: String,
) {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    data object NOTIFICATION : Permission(
        permission = Manifest.permission.POST_NOTIFICATIONS,
        title = "Notification Permission",
        description = "This permission is required to retrieve notification before an event start.",
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionAlertDialog(
    neededPermission: NeededPermission,
    isPermissionDeclined: Boolean,
    onDismiss: () -> Unit,
    onOkClick: () -> Unit,
    onGoToAppSettingsClick: () -> Unit,
) {
    val (openConfirmDialog, setOpenConfirmDialog) =
        remember {
            mutableStateOf(true)
        }

    ConfirmAlertDialog(
        openConfirmDialog = openConfirmDialog,
        setOpenConfirmDialog = setOpenConfirmDialog,
        title = neededPermission.title,
        text = neededPermission.description,
        confirmLabel = "Allow",
        onConfirm = {
            if (isPermissionDeclined) {
                onGoToAppSettingsClick()
            } else {
                onOkClick()
            }
        },
        dismissLabel = "Reject",
        onDismiss = onDismiss,
    )
}

enum class NeededPermission(
    val permission: String,
    val title: String,
    val description: String,
    val permanentlyDeniedDescription: String,
) {
    COARSE_LOCATION(
        permission = android.Manifest.permission.ACCESS_COARSE_LOCATION,
        title = "Approximate Location Permission",
        description = "This permission is needed to get your approximate location. Please grant the permission.",
        permanentlyDeniedDescription = "This permission is needed to get your approximate location. Please grant the permission in app settings.",
    ),

    READ_CALENDAR(
        permission = android.Manifest.permission.READ_CALENDAR,
        title = "Read Calendar Permission",
        description = "This permission is needed to read your calendar. Please grant the permission.",
        permanentlyDeniedDescription = "This permission is needed to read your calendar. Please grant the permission in app settings.",
    ),

    READ_CONTACTS(
        permission = android.Manifest.permission.READ_CONTACTS,
        title = "Read Contacts Permission",
        description = "This permission is needed to read your contacts. Please grant the permission.",
        permanentlyDeniedDescription = "This permission is needed to read your contacts. Please grant the permission in app settings.",
    ),

    RECORD_AUDIO(
        permission = android.Manifest.permission.RECORD_AUDIO,
        title = "Record Audio permission",
        description = "This permission is needed to access your microphone. Please grant the permission.",
        permanentlyDeniedDescription = "This permission is needed to access your microphone. Please grant the permission in app settings.",
    ),
    ;

    fun permissionTextProvider(isPermanentDenied: Boolean): String {
        return if (isPermanentDenied) this.permanentlyDeniedDescription else this.description
    }
}

fun getNeededPermission(permission: String): NeededPermission {
    return NeededPermission.values().find {
        it.permission == permission
    } ?: throw IllegalArgumentException("Permission $permission is not supported")
}

@Composable
fun Permissions() {
    val activity = LocalContext.current as Activity

    var permissionDialog by
        remember {
            mutableStateOf(mutableListOf<NeededPermission>())
        }

    val microphonePermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                if (!isGranted) {
                    permissionDialog.add(NeededPermission.RECORD_AUDIO)
                }
            },
        )

    val multiplePermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
            onResult = { permissions ->
                permissions.entries.forEach { entry ->
                    if (!entry.value) {
                        permissionDialog.add(getNeededPermission(entry.key))
                    }
                }
            },
        )

    Column(
        modifier =
            Modifier
                .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.spacedBy(
                16.dp,
                Alignment.CenterVertically,
            ),
    ) {
        Button(
            onClick = {
                microphonePermissionLauncher.launch(NeededPermission.RECORD_AUDIO.permission)
            },
        ) {
            Text(text = "Request bluetooth Permission")
        }

        Button(
            onClick = {
                multiplePermissionLauncher.launch(
                    arrayOf(
                        NeededPermission.COARSE_LOCATION.permission,
                        NeededPermission.READ_CALENDAR.permission,
                        NeededPermission.READ_CONTACTS.permission,
                    ),
                )
            },
        ) {
            Text(text = "Request multiple Permissions")
        }
    }

    permissionDialog.forEach { permission ->
        PermissionAlertDialog(
            neededPermission = permission,
            onDismiss = { permissionDialog.remove(permission) },
            onOkClick = {
                permissionDialog.remove(permission)
                multiplePermissionLauncher.launch(arrayOf(permission.permission))
            },
            onGoToAppSettingsClick = {
                permissionDialog.remove(permission)
                activity.goToAppSetting()
            },
            isPermissionDeclined = !activity.shouldShowRequestPermissionRationale(permission.permission),
        )
    }
}

fun Activity.goToAppSetting() {
    val i =
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null),
        )
    startActivity(i)
}
