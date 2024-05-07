package dk.scheduling.schedulingfrontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import dk.scheduling.schedulingfrontend.datasources.AccountDataSource
import dk.scheduling.schedulingfrontend.datasources.api.getApiClient
import dk.scheduling.schedulingfrontend.gui.pages.AccountPage
import dk.scheduling.schedulingfrontend.gui.pages.CreateDevicePage
import dk.scheduling.schedulingfrontend.gui.pages.CreateTaskPage
import dk.scheduling.schedulingfrontend.gui.pages.HomePage
import dk.scheduling.schedulingfrontend.gui.pages.LoginPage
import dk.scheduling.schedulingfrontend.gui.pages.Page
import dk.scheduling.schedulingfrontend.gui.pages.SignUpPage
import dk.scheduling.schedulingfrontend.gui.pages.TaskOverviewPage
import dk.scheduling.schedulingfrontend.gui.theme.SchedulingFrontendTheme
import dk.scheduling.schedulingfrontend.repositories.account.AccountRepository
import dk.scheduling.schedulingfrontend.repositories.device.DeviceRepository
import dk.scheduling.schedulingfrontend.repositories.event.EventRepository
import dk.scheduling.schedulingfrontend.repositories.overviews.OverviewRepository
import dk.scheduling.schedulingfrontend.repositories.task.TaskRepository
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
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

                val startDestinationPage =
                    if (runBlocking { App.appModule.accountRepo.isLoggedIn() }) {
                        Page.DeviceOverview
                    } else {
                        Page.LoginPage
                    }

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
                        composable(Page.DeviceOverview.route) {
                            HomePage(overviewRepository = App.appModule.overviewRepo, deviceRepository = App.appModule.deviceRepo)
                        }
                        composable(Page.CreateDevicePage.route) {
                            CreateDevicePage(
                                deviceRepository = App.appModule.deviceRepo,
                                navigateOnValidCreation = { appState.navHostController.navigate(Page.DeviceOverview.route) },
                                navigateOnCancelCreation = {
                                    appState.navHostController.navigate(
                                        Page.DeviceOverview.route,
                                    )
                                },
                            )
                        }
                        composable(
                            Page.TaskOverview.route,
                        ) { TaskOverviewPage(overviewRepository = App.appModule.overviewRepo, taskRepository = App.appModule.taskRepo) }
                        composable(Page.CreateTaskPage.route) {
                            CreateTaskPage(
                                deviceRepository = App.appModule.deviceRepo,
                                taskRepository = App.appModule.taskRepo,
                                navigateOnValidCreation = { appState.navHostController.navigate(Page.TaskOverview.route) },
                                navigateOnCancelCreation = {
                                    appState.navHostController.navigate(
                                        Page.TaskOverview.route,
                                    )
                                },
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
