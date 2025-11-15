package com.msa.seeyoulater.ui.screens.main.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BatchImportDialog(
    onDismiss: () -> Unit,
    onImport: (String) -> Unit
) {
    var urlText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Batch Import URLs") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Paste multiple URLs below (one per line):",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = urlText,
                    onValueChange = { urlText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    placeholder = { Text("https://example.com\nhttps://another-example.com") },
                    maxLines = 10
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (urlText.isNotBlank()) {
                        onImport(urlText)
                        onDismiss()
                    }
                },
                enabled = urlText.isNotBlank()
            ) {
                Text("Import")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
