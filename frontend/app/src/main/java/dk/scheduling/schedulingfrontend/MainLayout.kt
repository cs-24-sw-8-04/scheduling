package dk.scheduling.schedulingfrontend

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dk.scheduling.schedulingfrontend.ui.theme.SchedulingFrontendTheme

@Composable
fun App() {
    var currentPageNumber by remember { mutableStateOf(PageNumber.HomePage) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Content of the current page
        Column(modifier = Modifier.fillMaxSize()) {
            pagesInfo.find { it.pageNumber == currentPageNumber }?.composable?.invoke() ?: HomePage() // Default to HomePage if not found
            Spacer(modifier = Modifier.weight(1f))
        }

        // Bottom navigation bar
        BottomNavigationBar(
            currentPageNumber = currentPageNumber,
            onPageSelected = { page -> currentPageNumber = page },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
fun BottomNavigationBar(
    currentPageNumber: PageNumber,
    onPageSelected: (PageNumber) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(modifier = modifier) {
        pagesInfo.forEach { page ->
            NavigationBarItem(
                icon = { Icon(page.icon, contentDescription = page.description) },
                label = { Text(page.description) },
                selected = currentPageNumber == page.pageNumber,
                onClick = { onPageSelected(page.pageNumber) },
            )
        }
    }
}

@Preview(showBackground = true, device = "spec:id=reference_phone,shape=Normal,width=411,height=891,unit=dp,dpi=420")
@Composable
fun PreviewApp() {
    SchedulingFrontendTheme {
        App()
    }
}

@Preview(showBackground = true, device = "spec:id=reference_phone,shape=Normal,width=411,height=891,unit=dp,dpi=420")
@Composable
fun PreviewAppLightMode() {
    SchedulingFrontendTheme(darkTheme = false, dynamicColor = false) {
        App()
    }
}

@Preview(showBackground = true, device = "spec:id=reference_phone,shape=Normal,width=411,height=891,unit=dp,dpi=420")
@Composable
fun PreviewAppDarkMode() {
    SchedulingFrontendTheme(darkTheme = true, dynamicColor = false) {
        App()
    }
}
