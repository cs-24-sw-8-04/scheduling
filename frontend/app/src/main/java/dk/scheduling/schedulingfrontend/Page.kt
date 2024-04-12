package dk.scheduling.schedulingfrontend

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Page(val route: String, val icon: ImageVector, val description: String) {
    data object Home : Page("Home", Icons.Default.Home, "Home")

    data object ApiButton : Page("ApiButton", Icons.Default.Favorite, "Api Button")

    data object Page3 : Page("Settings", Icons.Default.Settings, "Settings")
}
