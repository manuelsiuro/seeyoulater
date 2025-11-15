package com.msa.seeyoulater.ui.screens.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.msa.seeyoulater.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    // Simulate loading or initialization; navigate immediately if no work needed
    // Avoid artificial delays unless absolutely necessary for branding.
    val splashDurationMillis = 500L // Keep short

    LaunchedEffect(Unit) {
        delay(splashDurationMillis)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Replace with your actual App Logo if available
             // Image(painter = painterResource(id = R.drawable.ic_launcher_foreground), contentDescription = "App Logo")
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(32.dp))
            CircularProgressIndicator()
        }
    }
}
