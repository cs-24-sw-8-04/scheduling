import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import dk.scheduling.schedulingfrontend.HomePage
import dk.scheduling.schedulingfrontend.Page
import dk.scheduling.schedulingfrontend.pagesInfo

@Composable
fun App() {
    var currentPage by remember { mutableStateOf(Page.HomePage) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Content of the current page
        Column(modifier = Modifier.fillMaxSize()) {
            pagesInfo.find { it.page == currentPage }?.composable?.invoke() ?: HomePage() // Default to HomePage if not found
            Spacer(modifier = Modifier.weight(1f))
        }

        // Bottom navigation bar
        BottomNavigationBar(
            currentPage = currentPage,
            onPageSelected = { page -> currentPage = page },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
fun BottomNavigationBar(
    currentPage: Page,
    onPageSelected: (Page) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(color = Color.Gray, modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            pagesInfo.forEach { pageInfo ->
                BottomNavigationButton(
                    icon = pageInfo.icon,
                    isSelected = currentPage == pageInfo.page,
                ) { onPageSelected(pageInfo.page) }
            }
        }
    }
}

@Composable
fun BottomNavigationButton(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) Color.White else Color.Magenta,
        )
    }
}

@Preview(showBackground = true, device = "spec:id=reference_phone,shape=Normal,width=411,height=891,unit=dp,dpi=420")
@Composable
fun PreviewApp() {
    App()
}
