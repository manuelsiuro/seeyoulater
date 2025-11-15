package com.msa.seeyoulater.ui.screens.statistics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars),
        topBar = {
            TopAppBar(
                title = { Text("Statistics") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Navigate back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                StatisticsContent(
                    statistics = state.statistics,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun StatisticsContent(
    statistics: StatisticsData,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Overview Card
        StatisticsCard(
            title = "Overview",
            icon = Icons.Default.Dashboard
        ) {
            StatisticRow(
                label = "Total Links",
                value = statistics.totalLinks.toString(),
                icon = Icons.Default.Link
            )
            StatisticRow(
                label = "Tags",
                value = statistics.totalTags.toString(),
                icon = Icons.Default.Label
            )
            StatisticRow(
                label = "Collections",
                value = statistics.totalCollections.toString(),
                icon = Icons.Default.Folder
            )
        }

        // Links Card
        StatisticsCard(
            title = "Links",
            icon = Icons.Default.Link
        ) {
            StatisticRow(
                label = "Starred",
                value = statistics.starredLinks.toString(),
                percentage = statistics.starredPercentage,
                icon = Icons.Default.Star
            )
            StatisticRow(
                label = "Opened",
                value = statistics.openedLinks.toString(),
                percentage = statistics.openedPercentage,
                icon = Icons.Default.Visibility
            )
            StatisticRow(
                label = "Unread",
                value = statistics.unreadLinks.toString(),
                icon = Icons.Default.VisibilityOff
            )
        }

        // Content Card
        StatisticsCard(
            title = "Content",
            icon = Icons.Default.Article
        ) {
            StatisticRow(
                label = "Saved for Offline",
                value = statistics.linksWithSavedContent.toString(),
                percentage = statistics.savedContentPercentage,
                icon = Icons.Default.CloudDone
            )
            StatisticRow(
                label = "With Notes",
                value = statistics.linksWithNotes.toString(),
                percentage = statistics.notesPercentage,
                icon = Icons.Default.Note
            )
        }
    }
}

@Composable
private fun StatisticsCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            HorizontalDivider()
            content()
        }
    }
}

@Composable
private fun StatisticRow(
    label: String,
    value: String,
    percentage: Float? = null,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            if (percentage != null && percentage > 0) {
                Text(
                    text = "(${String.format("%.0f", percentage)}%)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
