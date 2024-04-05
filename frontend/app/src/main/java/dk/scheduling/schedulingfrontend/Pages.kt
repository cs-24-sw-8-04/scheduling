package dk.scheduling.schedulingfrontend

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

enum class PageNumber {
    HomePage,
    ApiButtonPage,
    PAGE_3,
}

data class PageInfo(
    val pageNumber: PageNumber,
    val icon: ImageVector,
    val description: String,
    val composable: @Composable () -> Unit,
)

val pagesInfo =
    listOf(
        PageInfo(PageNumber.HomePage, Icons.Default.Home, "Home") { HomePage() },
        PageInfo(PageNumber.ApiButtonPage, Icons.Default.Favorite, "Favorite") { ApiButton() },
        PageInfo(PageNumber.PAGE_3, Icons.Default.Settings, "Settings") { Page3() },
    )
