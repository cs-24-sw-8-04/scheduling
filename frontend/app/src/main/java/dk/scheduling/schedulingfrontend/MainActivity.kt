package dk.scheduling.schedulingfrontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dk.scheduling.schedulingfrontend.pages.AccountPage
import dk.scheduling.schedulingfrontend.pages.ApiButton
import dk.scheduling.schedulingfrontend.pages.HomePage
import dk.scheduling.schedulingfrontend.pages.LoginPage
import dk.scheduling.schedulingfrontend.pages.Page
import dk.scheduling.schedulingfrontend.pages.Page3
import dk.scheduling.schedulingfrontend.pages.SignUpPage
import dk.scheduling.schedulingfrontend.repositories.AccountDataSource
import dk.scheduling.schedulingfrontend.repositories.AccountRepository
import dk.scheduling.schedulingfrontend.ui.theme.SchedulingFrontendTheme
import kotlinx.coroutines.runBlocking
import testdata.testDeviceOverview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val accountDataStorage = AccountDataSource(this)
            val accountRepo = AccountRepository(accountDataSource = accountDataStorage)

            SchedulingFrontendTheme {
                val appState = rememberAppState()

                val pages =
                    listOf(
                        Page.Home,
                        Page.ApiButton,
                        Page.CreateTaskPage,
                        Page.Account,
                    )

                val startDestinationPage = if (runBlocking { accountRepo.isLoggedIn() }) Page.Home else Page.LoginPage

                Scaffold(
                    bottomBar = {
                        if (appState.shouldShowBottomBar) {
                            BottomNavigationBar(navController = appState.navHostController, pages = pages)
                        }
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
                                navigateOnValidLogIn = { appState.navHostController.navigate(Page.Home.route) },
                                navigateToSignUpPage = { appState.navHostController.navigate(Page.SignUpPage.route) },
                            )
                        }
                        composable(Page.SignUpPage.route) {
                            SignUpPage(
                                accountRepo = accountRepo,
                                navigateOnValidSignUp = { appState.navHostController.navigate(Page.Home.route) },
                                navigateToLoginPage = { appState.navHostController.navigate(Page.LoginPage.route) },
                            )
                        }
                        composable(Page.Home.route) { HomePage(modifier = Modifier, getDevices = { testDeviceOverview() }) }
                        composable(Page.ApiButton.route) { ApiButton() }
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
    private val routes =
        listOf(
            Page.Home,
            Page.ApiButton,
            Page.Page3,
        ).map { it.route }

    val shouldShowBottomBar: Boolean
        @Composable get() =
            navHostController.currentBackStackEntryAsState().value?.destination?.route in routes
}

@Composable
fun rememberAppState(navHostController: NavHostController = rememberNavController()) =
    remember(navHostController) {
        AppState(navHostController)
    }
