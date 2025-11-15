package com.msa.seeyoulater.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Main : Screen("main")
    object Settings : Screen("settings")
    object Collections : Screen("collections")
    object Tags : Screen("tags")
    object Detail : Screen("detail/{linkId}") {
        fun createRoute(linkId: Long) = "detail/$linkId"
    }
    object Reader : Screen("reader/{linkId}") {
        fun createRoute(linkId: Long) = "reader/$linkId"
    }
    object Statistics : Screen("statistics")
}
