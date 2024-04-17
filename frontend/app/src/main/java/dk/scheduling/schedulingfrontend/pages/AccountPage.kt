package dk.scheduling.schedulingfrontend.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import dk.scheduling.schedulingfrontend.components.FilledButton
import dk.scheduling.schedulingfrontend.components.OutlinedButton
import dk.scheduling.schedulingfrontend.repositories.IAccountRepository
import dk.scheduling.schedulingfrontend.ui.theme.SchedulingFrontendTheme
import kotlinx.coroutines.launch
import testdata.DummyAccountRepository

@Composable
fun AccountPage(
    modifier: Modifier = Modifier,
    navigateOnLogout: () -> Unit,
    accountRepo: IAccountRepository,
) {
    val coroutineScope = rememberCoroutineScope()

    var username by remember {
        mutableStateOf("")
    }

    var loaded by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(loaded) {
        username = accountRepo.getUsername()
        loaded = true
    }

    if (loaded) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 75.dp).padding(top = 150.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Hi $username",
                fontSize = TextUnit(10f, TextUnitType.Em),
                textAlign = TextAlign.Center,
                lineHeight = TextUnit(1f, TextUnitType.Em),
            )
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 75.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LogoutButton(logout = {
                coroutineScope.launch {
                    accountRepo.logout()
                    navigateOnLogout()
                }
            })
        }
    }
}

@Composable
fun LogoutButton(logout: () -> Unit) {
    var openConfirmDialog by remember { mutableStateOf(false) }

    if (openConfirmDialog) {
        AlertDialog(
            title = {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Logout?",
                    textAlign = TextAlign.Center,
                )
            },
            text = {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Are you sure that you want to logout?",
                    textAlign = TextAlign.Center,
                )
            },
            confirmButton = {
                FilledButton(
                    onClick = {
                        openConfirmDialog = false
                        logout()
                    },
                    text = "Confirm",
                )
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        openConfirmDialog = false
                    },
                    text = "Cancel",
                )
            },
            onDismissRequest = { openConfirmDialog = false },
        )
    }

    FilledButton(
        text = "Logout",
        onClick = {
            openConfirmDialog = true
        },
    )
}

@Preview(showBackground = true, device = "spec:id=reference_phone,shape=Normal,width=411,height=891,unit=dp,dpi=420")
@Composable
fun PreviewAppDarkMode() {
    SchedulingFrontendTheme(darkTheme = true, dynamicColor = false) {
        AccountPage(navigateOnLogout = {}, accountRepo = DummyAccountRepository())
    }
}
