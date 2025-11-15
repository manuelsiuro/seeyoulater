package com.msa.seeyoulater.ui.screens.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.msa.seeyoulater.data.local.entity.Link

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkActionBottomSheet(
    link: Link,
    onDismiss: () -> Unit,
    onOpenLink: () -> Unit,
    onEditLink: () -> Unit,
    onShareLink: () -> Unit,
    onToggleRead: () -> Unit,
    onToggleStar: () -> Unit,
    onDeleteLink: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            // Link Preview Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Favicon
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(link.faviconUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Link favicon",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Link Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = link.title ?: link.url,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!link.title.isNullOrBlank()) {
                        Text(
                            text = link.url,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            Spacer(modifier = Modifier.height(8.dp))

            // Action Items
            ActionItem(
                icon = Icons.Default.OpenInBrowser,
                title = "Open in Browser",
                subtitle = "Open this link",
                onClick = {
                    onOpenLink()
                    onDismiss()
                }
            )

            ActionItem(
                icon = Icons.Default.Edit,
                title = "Edit Details",
                subtitle = "Edit title and description",
                onClick = {
                    onEditLink()
                    onDismiss()
                }
            )

            ActionItem(
                icon = if (link.isOpened) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                title = if (link.isOpened) "Mark as Unread" else "Mark as Read",
                subtitle = if (link.isOpened) "Mark this link as unread" else "Mark this link as read",
                onClick = {
                    onToggleRead()
                    onDismiss()
                }
            )

            ActionItem(
                icon = if (link.isStarred) Icons.Filled.Star else Icons.Outlined.StarBorder,
                title = if (link.isStarred) "Remove from Favorites" else "Add to Favorites",
                subtitle = if (link.isStarred) "Remove star" else "Star this link",
                iconTint = if (link.isStarred) MaterialTheme.colorScheme.primary else null,
                onClick = {
                    onToggleStar()
                    onDismiss()
                }
            )

            ActionItem(
                icon = Icons.Default.Share,
                title = "Share Link",
                subtitle = "Share via other apps",
                onClick = {
                    onShareLink()
                    onDismiss()
                }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

            ActionItem(
                icon = Icons.Default.Delete,
                title = "Delete",
                subtitle = "Remove this link",
                iconTint = MaterialTheme.colorScheme.error,
                onClick = {
                    onDeleteLink()
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun ActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    iconTint: androidx.compose.ui.graphics.Color? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(24.dp),
            tint = iconTint ?: LocalContentColor.current
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
