package com.volvoxhub.mediakit.ui.alerts

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.volvoxhub.mediakit.R
import com.volvoxhub.mediakit.ui.theme.MediaKitTheme

@Composable
fun PermissionAlertDialog(
    permission: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.permission_denied_title, permission)) },
        text = { Text(text = stringResource(R.string.permission_denied_message, permission)) },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(text = stringResource(id = R.string.settings_text))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.ok_text))
            }
        }
    )
}

@Preview(showBackground = true, locale = "en")
@Composable
fun PermissionDeniedAlertDialog() {
    MediaKitTheme {
        PermissionAlertDialog(permission = stringResource(id = R.string.option_camera), onConfirm = { } ) {
            
        }
    }
}