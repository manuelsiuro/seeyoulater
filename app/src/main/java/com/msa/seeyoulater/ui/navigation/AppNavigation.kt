package com.msa.seeyoulater.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.msa.seeyoulater.LinkManagerApp
import com.msa.seeyoulater.ui.screens.detail.LinkDetailScreen
import com.msa.seeyoulater.ui.screens.detail.LinkDetailViewModel
import com.msa.seeyoulater.ui.screens.main.MainScreen
import com.msa.seeyoulater.ui.screens.settings.SettingsScreen
import com.msa.seeyoulater.ui.screens.settings.SettingsViewModel
import com.msa.seeyoulater.ui.screens.collections.CollectionsScreen
import com.msa.seeyoulater.ui.screens.collections.CollectionsViewModel
import com.msa.seeyoulater.ui.screens.tags.TagsScreen
import com.msa.seeyoulater.ui.screens.tags.TagsViewModel
import com.msa.seeyoulater.ui.screens.splash.SplashScreen
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@Composable
fun AppNavigation(navController: NavHostController) {
    // Simple ViewModel Factory for demonstration without Hilt
    val viewModelFactory = (navController.context.applicationContext as LinkManagerApp).viewModelFactory

    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(onTimeout = {
                navController.navigate(Screen.Main.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true } // Remove splash from back stack
                }
            })
        }
        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToDetail = { linkId ->
                    navController.navigate(Screen.Detail.createRoute(linkId))
                }
            )
        }
        composable(Screen.Settings.route) {
             val settingsViewModel: SettingsViewModel = viewModel(factory = viewModelFactory)
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCollections = { navController.navigate(Screen.Collections.route) },
                onNavigateToTags = { navController.navigate(Screen.Tags.route) }
            )
        }
        composable(Screen.Collections.route) {
            val collectionsViewModel: CollectionsViewModel = viewModel(factory = viewModelFactory)
            CollectionsScreen(
                viewModel = collectionsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Tags.route) {
            val tagsViewModel: TagsViewModel = viewModel(factory = viewModelFactory)
            TagsScreen(
                viewModel = tagsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument("linkId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val linkId = backStackEntry.arguments?.getLong("linkId") ?: return@composable
            val detailViewModel = LinkDetailViewModel(
                repository = (navController.context.applicationContext as LinkManagerApp).repository,
                linkId = linkId
            )
            LinkDetailScreen(
                viewModel = detailViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
