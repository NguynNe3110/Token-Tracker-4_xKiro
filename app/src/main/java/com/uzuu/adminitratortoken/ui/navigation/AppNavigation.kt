package com.uzuu.adminitratortoken.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.uzuu.adminitratortoken.ui.screens.dashboard.DashboardScreen
import com.uzuu.adminitratortoken.ui.screens.account.AccountListScreen
import com.uzuu.adminitratortoken.ui.screens.account.AddAccountScreen

object Routes {
    const val DASHBOARD = "dashboard"
    const val ACCOUNTS = "accounts"
    const val ADD_ACCOUNT = "add_account"
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.DASHBOARD
    ) {
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onNavigateToAccounts = { navController.navigate(Routes.ACCOUNTS) }
            )
        }
        composable(Routes.ACCOUNTS) {
            AccountListScreen(
                onNavigateToAddAccount = { navController.navigate(Routes.ADD_ACCOUNT) },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Routes.ADD_ACCOUNT) {
            AddAccountScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
