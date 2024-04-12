package dk.scheduling.schedulingfrontend

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dk.scheduling.schedulingfrontend.ui.theme.SchedulingFrontendTheme
import testdata.testDeviceOverview

@Composable
fun App() {
    val navController = rememberNavController()

    val pages =
        listOf(
            Page.Home,
            Page.ApiButton,
            Page.Page3,
        )

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController, pages = pages)
        },
    ) { innerPadding ->
        // Content of the current page
        NavHost(navController = navController, startDestination = Page.Home.route, modifier = Modifier.padding(innerPadding)) {
            composable(Page.Home.route) { HomePage(modifier = Modifier, getDevices = { testDeviceOverview() }) }
            composable(Page.ApiButton.route) { ApiButton() }
            composable(Page.Page3.route) { Page3() }
        }
    }
}

@Composable
fun BottomNavigationBar(
    navController: NavController,
    pages: List<Page>,
    modifier: Modifier = Modifier,
) {
    NavigationBar(modifier = modifier) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        pages.forEach { page ->
            NavigationBarItem(
                icon = { Icon(page.icon, contentDescription = page.description) },
                label = { Text(page.description) },
                selected = currentDestination?.hierarchy?.any { it.route == page.route } == true,
                onClick = {
                    navController.navigate(page.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                },
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
