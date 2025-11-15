package com.msa.seeyoulater.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.msa.seeyoulater.LinkManagerApp
import com.msa.seeyoulater.data.preferences.ThemeMode
import com.msa.seeyoulater.ui.navigation.AppNavigation
import com.msa.seeyoulater.ui.theme.LinkManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // Get theme preferences repository from application
            val app = application as LinkManagerApp
            val themeSettings by app.themePreferencesRepository.themeSettings.collectAsState(
                initial = com.msa.seeyoulater.data.preferences.ThemeSettings()
            )

            // Determine dark theme based on theme mode
            val systemDarkTheme = isSystemInDarkTheme()
            val darkTheme = when (themeSettings.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> systemDarkTheme
            }

            LinkManagerTheme(
                darkTheme = darkTheme,
                colorScheme = themeSettings.colorScheme
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavigation(navController = navController)
                }
            }
        }
    }
}
