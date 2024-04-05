package dk.scheduling.schedulingfrontend.signuppage

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dk.scheduling.schedulingfrontend.ui.theme.SchedulingFrontendTheme

@Composable
fun SignUpPage(
    modifier: Modifier = Modifier,
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
    var signupFail by remember {
        mutableStateOf(false)
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(all = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        StandardTextField(
            "Username",
            username,
            onValueChange = {
                username = it
                if (signupFail) signupFail = false
            },
        )

        Spacer(modifier = Modifier.height(20.dp))

        PasswordTextField(
            password,
            onPasswordChange = {
                password = it
                if (signupFail) signupFail = false
            },
        )

        Spacer(modifier = Modifier.height(20.dp))

        StandardTextField(
            "Home Address",
            homeAddress,
            onValueChange = {
                homeAddress = it
                if (signupFail) signupFail = false
            },
        )

        if (signupFail) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Wrong sign up information",
                color = MaterialTheme.colorScheme.error,
                style = TextStyle(fontSize = 15.sp),
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        Column {
            Button(onClick = {
                if (signUp(username, password, homeAddress)) {
                    navigateOnValidSignUp()
                } else {
                    signupFail = true
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Sign up")
            }
        }
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

@Composable
fun TextLabel(label: String) {
    Text(
        label,
        style = TextStyle(fontSize = 15.sp),
    )
}

@Composable
fun StandardTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    Column {
        TextLabel(label)
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .border(
                        color = Color.Black,
                        width = 1.dp,
                    ),
            singleLine = true,
        )
    }
}

@Composable
fun PasswordTextField(
    password: String,
    onPasswordChange: (String) -> Unit,
) {
    var showPassword by remember {
        mutableStateOf(false)
    }

    Column {
        TextLabel("Password")
        TextField(
            value = password,
            onValueChange = onPasswordChange,
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .border(
                        color = Color.Black,
                        width = 1.dp,
                    ),
            trailingIcon = {
                PasswordVisibilityToggleIcon(
                    showPassword = showPassword,
                    onTogglePasswordVisibility = { showPassword = !showPassword },
                )
            },
        )
    }
}

@Composable
fun PasswordVisibilityToggleIcon(
    showPassword: Boolean,
    onTogglePasswordVisibility: () -> Unit,
) {
    val image = if (showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
    val contentDescription = if (showPassword) "Hide password" else "Show password"

    IconButton(onClick = onTogglePasswordVisibility) {
        Icon(imageVector = image, contentDescription = contentDescription)
    }
}

fun signUp(
    username: String,
    password: String,
    homeAddress: String,
): Boolean {
    // TODO: Send to server and if the signup is valid return true
    return false
}

@Preview(showBackground = true, device = "spec:id=reference_phone,shape=Normal,width=411,height=891,unit=dp,dpi=420")
@Composable
fun SignUpPagePreviewLightMode() {
    SchedulingFrontendTheme(darkTheme = false) {
        SignUpPage(navigateOnValidSignUp = {}, navigateToLoginPage = {})
    }
}

@Preview(showBackground = true, device = "spec:id=reference_phone,shape=Normal,width=411,height=891,unit=dp,dpi=420")
@Composable
fun SignUpPagePreviewDarkMode() {
    SchedulingFrontendTheme(darkTheme = true) {
        SignUpPage(navigateOnValidSignUp = {}, navigateToLoginPage = {})
    }
}
