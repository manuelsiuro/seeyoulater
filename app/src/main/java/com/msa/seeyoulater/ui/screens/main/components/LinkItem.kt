package com.msa.seeyoulater.ui.screens.main.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.msa.seeyoulater.R
import com.msa.seeyoulater.data.local.entity.Link
import com.msa.seeyoulater.ui.theme.OpenedIndicatorColor
import com.msa.seeyoulater.ui.theme.StarredColor


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LinkItem(
    link: Link,
    onOpenLink: (Link) -> Unit,
    onToggleStar: (Long) -> Unit,
    onDeleteLink: (Link) -> Unit,
    modifier: Modifier = Modifier,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onLongClick: (Link) -> Unit = {},
    onSelectionToggle: (Long) -> Unit = {}
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_link_confirmation_title)) },
            text = { Text(stringResource(R.string.delete_link_confirmation_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteLink(link)
                        showDeleteDialog = false
                    }
                ) {
                    Text(stringResource(R.string.delete_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel_button))
                }
            }
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) {
                        onSelectionToggle(link.id)
                    } else {
                        onOpenLink(link)
                    }
                },
                onLongClick = {
                    if (!isSelectionMode) {
                        onLongClick(link)
                    }
                }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 2.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column {
            // Optional Preview Image
            if (!link.previewImageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(link.previewImageUrl)
                        .crossfade(true)
                        // .placeholder(R.drawable.ic_placeholder_image) // Add placeholder drawable
                        // .error(R.drawable.ic_error_image) // Add error drawable
                        .build(),
                    contentDescription = stringResource(R.string.cd_link_preview_image),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp), // Adjust height as needed
                    contentScale = ContentScale.Crop,
                    // Optional: Add loading indicator
                    // onLoading = { CircularProgressIndicator() },
                    // onError = { /* Handle error state */ }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Checkbox for selection mode
                if (isSelectionMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onSelectionToggle(link.id) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }

                // Favicon or Placeholder
                 AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(link.faviconUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = stringResource(R.string.cd_link_favicon),
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )


                Spacer(modifier = Modifier.width(12.dp))

                // Link Title and URL
                Column(modifier = Modifier.weight(1.0f)) {
                    Text(
                        text = link.title ?: link.url, // Show title, fallback to URL
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (!link.title.isNullOrBlank()) { // Show URL below title if title exists
                        Text(
                            text = link.url,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // Optional Description
                    if (!link.description.isNullOrBlank()) {
                         Spacer(modifier = Modifier.height(4.dp))
                         Text(
                            text = link.description!!,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Action Buttons
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Star Button
                    IconButton(
                        onClick = { onToggleStar(link.id) },
                        modifier = Modifier.size(36.dp) // Smaller touch target is ok if inside larger item
                    ) {
                        Icon(
                            imageVector = if (link.isStarred) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = if (link.isStarred) stringResource(R.string.cd_unstar_link) else stringResource(R.string.cd_star_link),
                            tint = if (link.isStarred) StarredColor else LocalContentColor.current.copy(alpha = 0.6f)
                        )
                    }

                    // Opened Indicator (subtle)
                    if (link.isOpened) {
                        Spacer(modifier = Modifier.height(4.dp))
                         Icon(
                            imageVector = Icons.Filled.Visibility, // Or use a simple dot: Icons.Filled.Circle
                            contentDescription = stringResource(R.string.cd_link_opened_indicator),
                            tint = OpenedIndicatorColor,
                            modifier = Modifier.size(16.dp)
                        )
                         // Alternative: Small dot
                        /*
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(OpenedIndicatorColor, CircleShape)
                                .padding(top = 4.dp)
                         )
                         */
                    }
                }
            }

            // Bottom Action Row (Share, Delete) - Hidden in selection mode
            if (!isSelectionMode) {
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Row(
                     modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 0.dp), // Reduced vertical padding
                    horizontalArrangement = Arrangement.End, // Align buttons to the right
                    verticalAlignment = Alignment.CenterVertically
                ) {
                     // Share Button
                     IconButton(onClick = { shareLink(context, link.url) }) {
                         Icon(
                             Icons.Default.Share,
                             contentDescription = stringResource(R.string.cd_share_link),
                             tint = MaterialTheme.colorScheme.secondary
                         )
                     }

                     // Delete Button
                     IconButton(onClick = { showDeleteDialog = true }) {
                         Icon(
                             Icons.Default.DeleteOutline,
                             contentDescription = stringResource(R.string.cd_delete_link),
                             tint = MaterialTheme.colorScheme.error
                         )
                     }
                 }
            }
        }
    }
}

private fun shareLink(context: Context, url: String) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, url)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, null)
    context.startActivity(shareIntent)
}
