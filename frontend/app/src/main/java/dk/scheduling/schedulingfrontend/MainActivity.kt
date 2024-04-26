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
import dk.scheduling.schedulingfrontend.api.getApiClient
import dk.scheduling.schedulingfrontend.datasources.AccountDataSource
import dk.scheduling.schedulingfrontend.pages.AccountPage
import dk.scheduling.schedulingfrontend.pages.CreateDevicePage
import dk.scheduling.schedulingfrontend.pages.CreateTaskPage
import dk.scheduling.schedulingfrontend.pages.HomePage
import dk.scheduling.schedulingfrontend.pages.LoginPage
import dk.scheduling.schedulingfrontend.pages.Page
import dk.scheduling.schedulingfrontend.pages.SignUpPage
import dk.scheduling.schedulingfrontend.pages.TaskOverviewPage
import dk.scheduling.schedulingfrontend.repositories.account.AccountRepository
import dk.scheduling.schedulingfrontend.repositories.device.DeviceRepository
import dk.scheduling.schedulingfrontend.repositories.event.EventRepository
import dk.scheduling.schedulingfrontend.repositories.overviews.OverviewRepository
import dk.scheduling.schedulingfrontend.repositories.task.TaskRepository
import dk.scheduling.schedulingfrontend.ui.theme.SchedulingFrontendTheme
import kotlinx.coroutines.runBlocking
import testdata.deviceTaskTestData

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SchedulingFrontendTheme {
                val service = getApiClient(baseUrl = getString(R.string.base_url))
                val accountDataStorage = AccountDataSource(this)
                val accountRepo = AccountRepository(accountDataSource = accountDataStorage, service = service)
                val deviceRepo = DeviceRepository(accountRepository = accountRepo, service = service)
                val taskRepo = TaskRepository(accountRepository = accountRepo, service = service)
                val eventRepo = EventRepository(accountRepository = accountRepo, service = service)
                val overviewRepo =
                    OverviewRepository(
                        deviceRepository = deviceRepo,
                        taskRepository = taskRepo,
                        eventRepository = eventRepo,
                    )

                val appState = rememberAppState()

                val pages =
                    listOf(
                        Page.DeviceOverview,
                        Page.TaskOverview,
                        Page.Account,
                    )

                val startDestinationPage = if (runBlocking { accountRepo.isLoggedIn() }) Page.DeviceOverview else Page.LoginPage

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
                                accountRepo = accountRepo,
                                navigateOnValidLogin = { appState.navHostController.navigate(Page.DeviceOverview.route) },
                                navigateToSignUpPage = { appState.navHostController.navigate(Page.SignUpPage.route) },
                            )
                        }
                        composable(Page.SignUpPage.route) {
                            SignUpPage(
                                accountRepo = accountRepo,
                                navigateOnValidSignUp = { appState.navHostController.navigate(Page.DeviceOverview.route) },
                                navigateToLoginPage = { appState.navHostController.navigate(Page.LoginPage.route) },
                            )
                        }
                        composable(Page.DeviceOverview.route) { HomePage(modifier = Modifier, overviewRepository = overviewRepo) }
                        composable(Page.CreateDevicePage.route) {
                            CreateDevicePage(
                                navigateOnValidCreation = { appState.navHostController.navigate(Page.DeviceOverview.route) },
                                navigateOnCancelCreation = { appState.navHostController.navigate(Page.DeviceOverview.route) },
                            )
                        }
                        composable(
                            Page.TaskOverview.route,
                        ) { TaskOverviewPage(modifier = Modifier, getDeviceTasks = { deviceTaskTestData() }) }
                        composable(Page.CreateTaskPage.route) { CreateTaskPage(Modifier, handleSubmission = {}, handleCancellation = {}) }
                        composable(Page.Account.route) {
                            AccountPage(
                                modifier = Modifier,
                                accountRepo = accountRepo,
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
