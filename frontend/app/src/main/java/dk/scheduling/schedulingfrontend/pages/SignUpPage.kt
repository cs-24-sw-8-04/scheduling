package dk.scheduling.schedulingfrontend.pages

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
import dk.scheduling.schedulingfrontend.components.FilledButton
import dk.scheduling.schedulingfrontend.components.PasswordTextField
import dk.scheduling.schedulingfrontend.components.StandardTextField
import dk.scheduling.schedulingfrontend.components.Title
import dk.scheduling.schedulingfrontend.repositories.account.IAccountRepository
import dk.scheduling.schedulingfrontend.ui.theme.SchedulingFrontendTheme
import kotlinx.coroutines.launch
import testdata.DummyAccountRepository

@Composable
fun SignUpPage(
    modifier: Modifier = Modifier,
    accountRepo: IAccountRepository,
    navigateOnValidSignUp: () -> Unit,
    navigateToLoginPage: () -> Unit,
) {
    var username by remember {
        mutableStateOf("")
    }
    var password by remember {
        mutableStateOf("")
    }
    var homeAddress by remember {
        mutableStateOf("")
    }
    var isSignUpFailed by remember {
        mutableStateOf(false)
    }

    val coroutineScope = rememberCoroutineScope()

    Title(titleText = "Sign Up")

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(all = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        StandardTextField(
            label = "Username",
            value = username,
            onValueChange = {
                username = it
                if (isSignUpFailed) isSignUpFailed = false
            },
            isError = isSignUpFailed,
        )

        Spacer(modifier = Modifier.height(20.dp))

        PasswordTextField(
            password,
            onPasswordChange = {
                password = it
                if (isSignUpFailed) isSignUpFailed = false
            },
            isError = isSignUpFailed,
        )

        Spacer(modifier = Modifier.height(20.dp))

        StandardTextField(
            label = "Home Address",
            value = homeAddress,
            onValueChange = {
                homeAddress = it
                if (isSignUpFailed) isSignUpFailed = false
            },
            isError = isSignUpFailed,
        )

        if (isSignUpFailed) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Wrong sign up information",
                color = MaterialTheme.colorScheme.error,
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        FilledButton(
            onClick = {
                coroutineScope.launch {
                    if (accountRepo.signUp(username, password)) {
                        navigateOnValidSignUp()
                    } else {
                        isSignUpFailed = true
                    }
                }
            },
            text = "Sign up",
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
            Text("Have already an account?")
            TextButton(onClick = { navigateToLoginPage() }) { Text("Sign In") }
        }
    }
}

@Preview(showBackground = true, device = "spec:id=reference_phone,shape=Normal,width=411,height=891,unit=dp,dpi=420")
@Composable
fun SignUpPagePreviewLightMode() {
    SchedulingFrontendTheme(darkTheme = false, dynamicColor = false) {
        SignUpPage(navigateOnValidSignUp = {}, navigateToLoginPage = {}, accountRepo = DummyAccountRepository())
    }
}

@Preview(showBackground = true, device = "spec:id=reference_phone,shape=Normal,width=411,height=891,unit=dp,dpi=420")
@Composable
fun SignUpPagePreviewDarkMode() {
    SchedulingFrontendTheme(darkTheme = true, dynamicColor = false) {
        SignUpPage(navigateOnValidSignUp = {}, navigateToLoginPage = {}, accountRepo = DummyAccountRepository())
    }
}
