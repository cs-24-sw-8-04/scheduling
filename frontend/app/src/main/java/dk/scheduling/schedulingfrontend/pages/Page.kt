package dk.scheduling.schedulingfrontend.pages

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Task
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Page(val route: String, val icon: ImageVector, val description: String) {
    data object LoginPage : Page("Login", Icons.AutoMirrored.Filled.Login, "Login")

    data object SignUpPage : Page("SignUp", Icons.Default.HowToReg, "Sign Up")

    data object DeviceOverview : Page("DeviceOverview", Icons.Default.Home, "Device")

    data object CreateDevicePage : Page("CreateDevicePage", Icons.Default.Add, "Device")

    data object TaskOverview : Page("TaskOverview", Icons.Default.Task, "Tasks")

    data object ApiButton : Page("ApiButton", Icons.Default.Favorite, "Api Button")

    data object CreateTaskPage : Page("CreateTaskPage", Icons.Default.Settings, "Task")

    data object Account : Page("Account", Icons.Default.AccountCircle, "Account")
}
