package com.msa.seeyoulater.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Main : Screen("main")
    object Settings : Screen("settings")
    object Detail : Screen("detail/{linkId}") {
        fun createRoute(linkId: Long) = "detail/$linkId"
    }
}
