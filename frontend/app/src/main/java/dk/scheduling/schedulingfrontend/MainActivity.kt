package dk.scheduling.schedulingfrontend

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dk.scheduling.schedulingfrontend.pages.AccountPage
import dk.scheduling.schedulingfrontend.pages.CreateDevicePage
import dk.scheduling.schedulingfrontend.pages.CreateTaskPage
import dk.scheduling.schedulingfrontend.pages.HomePage
import dk.scheduling.schedulingfrontend.pages.LoginPage
import dk.scheduling.schedulingfrontend.pages.Page
import dk.scheduling.schedulingfrontend.pages.SignUpPage
import dk.scheduling.schedulingfrontend.pages.TaskOverviewPage
import dk.scheduling.schedulingfrontend.ui.theme.SchedulingFrontendTheme
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    private val appPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions(), {})

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SchedulingFrontendTheme {
                val appState = rememberAppState()

                val pages =
                    listOf(
                        Page.DeviceOverview,
                        Page.TaskOverview,
                        Page.Account,
                    )
                val startDestinationPage = if (runBlocking { App.appModule.accountRepo.isLoggedIn() }) Page.DeviceOverview else Page.LoginPage

                Scaffold(
                    bottomBar = {
                        if (appState.shouldShowBottomBar) {
                            BottomNavigationBar(navController = appState.navHostController, pages = pages)
                        }
                    },
                    floatingActionButton = {
                        val linkedPages =
                            mutableListOf(
                                Pair(Page.DeviceOverview, Page.CreateDevicePage),
                                Pair(Page.TaskOverview, Page.CreateTaskPage),
                            )

                        FloatingActionButtonLinkToCreatePage(
                            navController = appState.navHostController,
                            linkedPages = linkedPages,
                        )
                    },
                ) { innerPadding ->
                    NavHost(
                        navController = appState.navHostController,
                        startDestination = startDestinationPage.route,
                        modifier = Modifier.padding(innerPadding),
                    ) {
                        composable(Page.LoginPage.route) {
                            requestNotificationPermissionDialog()
                            LoginPage(
                                accountRepo = App.appModule.accountRepo,
                                navigateOnValidLogin = { appState.navHostController.navigate(Page.DeviceOverview.route) },
                                navigateToSignUpPage = { appState.navHostController.navigate(Page.SignUpPage.route) },
                            )
                        }
                        composable(Page.SignUpPage.route) {
                            SignUpPage(
                                accountRepo = App.appModule.accountRepo,
                                navigateOnValidSignUp = { appState.navHostController.navigate(Page.DeviceOverview.route) },
                                navigateToLoginPage = { appState.navHostController.navigate(Page.LoginPage.route) },
                            )
                        }
                        composable(Page.DeviceOverview.route) { HomePage(overviewRepository = App.appModule.overviewRepo) }
                        composable(Page.CreateDevicePage.route) {
                            CreateDevicePage(
                                deviceRepository = App.appModule.deviceRepo,
                                navigateOnValidCreation = { appState.navHostController.navigate(Page.DeviceOverview.route) },
                                navigateOnCancelCreation = { appState.navHostController.navigate(Page.DeviceOverview.route) },
                            )
                        }
                        composable(
                            Page.TaskOverview.route,
                        ) { TaskOverviewPage(overviewRepository = App.appModule.overviewRepo) }
                        composable(Page.CreateTaskPage.route) {
                            CreateTaskPage(
                                deviceRepository = App.appModule.deviceRepo,
                                taskRepository = App.appModule.taskRepo,
                                navigateOnValidCreation = { appState.navHostController.navigate(Page.TaskOverview.route) },
                                navigateOnCancelCreation = { appState.navHostController.navigate(Page.TaskOverview.route) },
                            )
                        }
                        composable(Page.Account.route) {
                            AccountPage(
                                accountRepo = App.appModule.accountRepo,
                                navigateOnLogout = { appState.navHostController.navigate(Page.LoginPage.route) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun requestNotificationPermissionDialog() {
    val requestPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
            } else {
                // Explain to the user that the feature is unavailable because the
                // feature requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
            }
        }

    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
}

class AppState(
    val navHostController: NavHostController,
) {
    private val bottomBarPages =
        listOf(
            Page.DeviceOverview,
            Page.TaskOverview,
            Page.Account,
        ).map { it.route }

    val shouldShowBottomBar: Boolean
        @Composable get() =
            navHostController.currentBackStackEntryAsState().value?.destination?.route in bottomBarPages
}

@Composable
fun rememberAppState(navHostController: NavHostController = rememberNavController()) =
    remember(navHostController) {
        AppState(navHostController)
    }

@Composable
fun FloatingActionButtonLinkToCreatePage(
    navController: NavHostController,
    linkedPages: List<Pair<Page, Page>>,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val linkedPage = linkedPages.find { it.first.route == currentDestination?.route }
    if (linkedPage != null) {
        ExtendedFloatingActionButton(
            onClick = { navController.navigate(linkedPage.second.route) },
            icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
            text = { Text(text = linkedPage.second.description) },
        )
    }
}
