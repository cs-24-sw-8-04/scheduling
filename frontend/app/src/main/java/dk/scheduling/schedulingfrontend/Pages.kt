package dk.scheduling.schedulingfrontend

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import testdata.testDeviceOverview

enum class PageNumber {
    HomePage,
    ApiButtonPage,
    PAGE_3,
}

data class PageInfo(
    val icon: ImageVector,
    val description: String,
    val composable: @Composable () -> Unit,
)

val pagesInfo =
    mapOf(
        PageNumber.HomePage to
            PageInfo(
                Icons.Default.Home,
                "Home",
            ) { HomePage(modifier = Modifier, getDevices = { testDeviceOverview() }) },
        PageNumber.ApiButtonPage to PageInfo(Icons.Default.Favorite, "Favorite") { ApiButton() },
        PageNumber.PAGE_3 to PageInfo(Icons.Default.Settings, "Settings") { Page3() },
    )
