package com.msa.seeyoulater.ui.screens.reader

import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.msa.seeyoulater.data.local.entity.Link

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    viewModel: ReaderViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show error as snackbar
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.link?.title ?: "Reader Mode",
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Navigate back")
                    }
                },
                actions = {
                    // Font size controls
                    IconButton(onClick = { viewModel.decreaseFontSize() }) {
                        Icon(Icons.Default.TextDecrease, "Decrease font size")
                    }
                    Text(
                        text = "${state.fontSize}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    IconButton(onClick = { viewModel.increaseFontSize() }) {
                        Icon(Icons.Default.TextIncrease, "Increase font size")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
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
            state.link == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = state.error ?: "Link not found",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = onNavigateBack) {
                            Text("Go Back")
                        }
                    }
                }
            }
            state.isFetchingContent -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "Extracting article content...",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            else -> {
                ReaderContent(
                    link = state.link!!,
                    fontSize = state.fontSize,
                    onScrollProgressChanged = { progress ->
                        viewModel.updateReadingProgress(progress)
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun ReaderContent(
    link: Link,
    fontSize: Int,
    onScrollProgressChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = MaterialTheme.colorScheme.surface.toArgb()
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val context = LocalContext.current

    Box(modifier = modifier.fillMaxSize()) {
        if (!link.savedContent.isNullOrBlank()) {
            // Display article content in WebView for better HTML rendering
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        webViewClient = WebViewClient()
                        settings.apply {
                            javaScriptEnabled = false // Security: disable JS for reader mode
                            builtInZoomControls = false
                            displayZoomControls = false
                        }
                    }
                },
                update = { webView ->
                    val htmlContent = createStyledHtml(
                        content = link.savedContent!!,
                        notes = link.notes,
                        fontSize = fontSize,
                        backgroundColor = backgroundColor,
                        textColor = textColor
                    )
                    webView.loadDataWithBaseURL(
                        null,
                        htmlContent,
                        "text/html",
                        "UTF-8",
                        null
                    )
                },
                modifier = Modifier.fillMaxSize()
            )

            // Show reading metadata at the bottom
            if (link.estimatedReadingTime != null) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    tonalElevation = 2.dp,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${link.estimatedReadingTime} min read",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LinearProgressIndicator(
                            progress = link.readingProgress,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${(link.readingProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            // No content available
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Article,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "No article content available",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "This link doesn't have saved content for offline reading.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Create styled HTML for reader mode with proper typography and spacing
 */
private fun createStyledHtml(
    content: String,
    notes: String?,
    fontSize: Int,
    backgroundColor: Int,
    textColor: Int
): String {
    val bgColor = String.format("#%06X", 0xFFFFFF and backgroundColor)
    val fgColor = String.format("#%06X", 0xFFFFFF and textColor)

    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
                body {
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                    font-size: ${fontSize}px;
                    line-height: 1.6;
                    color: $fgColor;
                    background-color: $bgColor;
                    padding: 20px;
                    margin: 0;
                    max-width: 700px;
                    margin: 0 auto;
                }
                h1, h2, h3, h4, h5, h6 {
                    margin-top: 24px;
                    margin-bottom: 16px;
                    font-weight: 600;
                    line-height: 1.25;
                }
                h1 { font-size: ${fontSize + 8}px; }
                h2 { font-size: ${fontSize + 6}px; }
                h3 { font-size: ${fontSize + 4}px; }
                p {
                    margin-bottom: 16px;
                    text-align: justify;
                }
                img {
                    max-width: 100%;
                    height: auto;
                    display: block;
                    margin: 20px auto;
                    border-radius: 8px;
                }
                a {
                    color: #1976D2;
                    text-decoration: none;
                }
                a:hover {
                    text-decoration: underline;
                }
                pre, code {
                    background-color: rgba(127, 127, 127, 0.1);
                    border-radius: 4px;
                    padding: 4px 8px;
                    font-family: 'Courier New', monospace;
                    font-size: ${fontSize - 2}px;
                }
                pre {
                    padding: 16px;
                    overflow-x: auto;
                }
                blockquote {
                    border-left: 4px solid #1976D2;
                    margin: 16px 0;
                    padding-left: 16px;
                    font-style: italic;
                    color: rgba($fgColor, 0.7);
                }
                ul, ol {
                    margin-bottom: 16px;
                    padding-left: 24px;
                }
                li {
                    margin-bottom: 8px;
                }
                hr {
                    border: none;
                    border-top: 1px solid rgba(127, 127, 127, 0.2);
                    margin: 24px 0;
                }
                .notes-section {
                    background-color: rgba(255, 193, 7, 0.1);
                    border-left: 4px solid #FFC107;
                    padding: 16px;
                    margin-bottom: 24px;
                    border-radius: 4px;
                }
                .notes-header {
                    font-weight: 600;
                    font-size: ${fontSize}px;
                    margin-bottom: 12px;
                    color: #F57C00;
                }
                .notes-content {
                    font-size: ${fontSize - 1}px;
                    line-height: 1.5;
                    white-space: pre-wrap;
                    word-wrap: break-word;
                }
            </style>
        </head>
        <body>
            ${if (!notes.isNullOrBlank()) """
                <div class="notes-section">
                    <div class="notes-header">📝 Your Notes</div>
                    <div class="notes-content">$notes</div>
                </div>
            """ else ""}
            $content
        </body>
        </html>
    """.trimIndent()
}
