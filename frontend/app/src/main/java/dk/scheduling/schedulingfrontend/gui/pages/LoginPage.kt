package dk.scheduling.schedulingfrontend.gui.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dk.scheduling.schedulingfrontend.gui.components.FilledButton
import dk.scheduling.schedulingfrontend.gui.components.PasswordTextField
import dk.scheduling.schedulingfrontend.gui.components.StandardTextField
import dk.scheduling.schedulingfrontend.gui.components.Title
import dk.scheduling.schedulingfrontend.gui.theme.SchedulingFrontendTheme
import dk.scheduling.schedulingfrontend.repositories.account.IAccountRepository
import kotlinx.coroutines.launch
import testdata.DummyAccountRepository

@Composable
fun LoginPage(
    modifier: Modifier = Modifier,
    accountRepo: IAccountRepository,
    navigateOnValidLogin: () -> Unit,
    navigateToSignUpPage: () -> Unit,
) {
    var username by remember {
        mutableStateOf("")
    }
    var password by remember {
        mutableStateOf("")
    }
    var isLoginFailed by remember {
        mutableStateOf(false)
    }

    val coroutineScope = rememberCoroutineScope()

    Title(titleText = "Login")
    // Arranges the fields and the button in vertical sequence.
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(50.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Input field for username.
        StandardTextField(
            label = "Username",
            value = username,
            onValueChange = {
                username = it // it holds the value of the text field.
                if (isLoginFailed) isLoginFailed = false
            },
            isError = isLoginFailed,
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Input field for password.
        PasswordTextField(
            password,
            onPasswordChange = {
                password = it // it holds the value of the text field.
                if (isLoginFailed) isLoginFailed = false
            },
            isError = isLoginFailed,
        )

        if (isLoginFailed) { // Shows an error message if the login fails
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Wrong username or password",
                color = MaterialTheme.colorScheme.error,
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        FilledButton(
            onClick = {
                coroutineScope.launch { // Runs asynchronous
                    if (accountRepo.login(username, password)) {
                        navigateOnValidLogin()
                    } else {
                        isLoginFailed = true
                    }
                }
            },
            text = "Log In",
            enabled = username.isNotBlank() && password.isNotBlank(),
        )
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(50.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
    ) {
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text("Don't have an account?")
            TextButton(onClick = { navigateToSignUpPage() }) { Text("Sign Up") }
        }
    }
}

@Preview(showBackground = true, device = "spec:id=reference_phone,shape=Normal,width=411,height=891,unit=dp,dpi=420")
@Composable
fun LoginPagePreviewLightMode() {
    SchedulingFrontendTheme(darkTheme = false, dynamicColor = false) {
        LoginPage(navigateToSignUpPage = {}, navigateOnValidLogin = {}, accountRepo = DummyAccountRepository())
    }
}

@Preview(showBackground = true, device = "spec:id=reference_phone,shape=Normal,width=411,height=891,unit=dp,dpi=420")
@Composable
fun LoginPagePreviewDarkMode() {
    SchedulingFrontendTheme(darkTheme = true, dynamicColor = false) {
        LoginPage(navigateToSignUpPage = {}, navigateOnValidLogin = {}, accountRepo = DummyAccountRepository())
    }
}
