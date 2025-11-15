package com.msa.seeyoulater.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.msa.seeyoulater.data.local.entity.Tag

/**
 * A chip component for displaying a tag
 *
 * @param tag The tag to display
 * @param onRemove Optional callback when the remove icon is clicked (for editing mode)
 * @param modifier Optional modifier
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagChip(
    tag: Tag,
    onRemove: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val backgroundColor = tag.color?.let { parseColor(it) }
        ?: MaterialTheme.colorScheme.secondaryContainer
    val contentColor = if (tag.color != null && parseColor(tag.color) != null) {
        getContrastColor(parseColor(tag.color)!!)
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }

    FilterChip(
        selected = false,
        onClick = { /* Tags are not clickable by default */ },
        label = {
            Text(
                text = tag.name,
                style = MaterialTheme.typography.labelMedium
            )
        },
        trailingIcon = if (onRemove != null) {
            {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(18.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove tag",
                        modifier = Modifier.size(16.dp),
                        tint = contentColor
                    )
                }
            }
        } else null,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = backgroundColor,
            labelColor = contentColor,
            iconColor = contentColor
        ),
        modifier = modifier.padding(end = 4.dp)
    )
}

/**
 * Parse hex color string to Color
 * Returns null if parsing fails
 */
private fun parseColor(colorString: String): Color? {
    return try {
        Color(android.graphics.Color.parseColor(colorString))
    } catch (e: Exception) {
        null
    }
}

/**
 * Get contrasting text color (black or white) for the given background color
 */
private fun getContrastColor(backgroundColor: Color): Color {
    // Calculate relative luminance
    val red = backgroundColor.red
    val green = backgroundColor.green
    val blue = backgroundColor.blue

    val luminance = 0.299 * red + 0.587 * green + 0.114 * blue

    // Use white text for dark backgrounds, black for light backgrounds
    return if (luminance > 0.5) Color.Black else Color.White
}
