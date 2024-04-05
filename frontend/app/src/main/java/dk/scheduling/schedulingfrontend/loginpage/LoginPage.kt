package dk.scheduling.schedulingfrontend.loginpage

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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dk.scheduling.schedulingfrontend.sharedcomponents.FilledButton
import dk.scheduling.schedulingfrontend.sharedcomponents.PasswordTextField
import dk.scheduling.schedulingfrontend.sharedcomponents.StandardTextField
import dk.scheduling.schedulingfrontend.sharedcomponents.Title
import dk.scheduling.schedulingfrontend.ui.theme.SchedulingFrontendTheme

@Composable
fun LoginPage(
    modifier: Modifier = Modifier,
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

    Title(titleText = "Login")

    
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(50.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        StandardTextField(
            label = "Username",
            value = username,
            onValueChange = {
                username = it
                if (isLoginFailed) isLoginFailed = false
            },
            isError = isLoginFailed,
        )

        Spacer(modifier = Modifier.height(20.dp))

        PasswordTextField(
            password,
            onPasswordChange = {
                password = it
                if (isLoginFailed) isLoginFailed = false
            },
            isError = isLoginFailed,
        )

        if (isLoginFailed) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Wrong username or password",
                color = MaterialTheme.colorScheme.error,
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        FilledButton(
            onClick = {
                if (login(username, password)) {
                    navigateOnValidLogin()
                } else {
                    isLoginFailed = true
                }
            },
            text = "Log In",
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

fun login(
    username: String,
    password: String,
): Boolean {
    // TODO: Send to server and if the login is valid return true
    return false
}

@Preview(showBackground = true, device = "spec:id=reference_phone,shape=Normal,width=411,height=891,unit=dp,dpi=420")
@Composable
fun LoginPagePreviewLightMode() {
    SchedulingFrontendTheme(darkTheme = false, dynamicColor = false) {
        LoginPage(navigateOnValidLogin = {}, navigateToSignUpPage = {})
    }
}

@Preview(showBackground = true, device = "spec:id=reference_phone,shape=Normal,width=411,height=891,unit=dp,dpi=420")
@Composable
fun LoginPagePreviewDarkMode() {
    SchedulingFrontendTheme(darkTheme = true, dynamicColor = false) {
        LoginPage(navigateOnValidLogin = {}, navigateToSignUpPage = {})
    }
}
