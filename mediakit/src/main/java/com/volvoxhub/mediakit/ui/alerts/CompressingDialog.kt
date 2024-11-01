package com.volvoxhub.mediakit.ui.alerts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.volvoxhub.mediakit.R
import com.volvoxhub.mediakit.ui.theme.MediaKitTheme

@Composable
fun ShowCompressionPopupDialog() {
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {},
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator()
                Text(stringResource(id = R.string.video_compressing_text))
            }
        }
    )
}

@Preview(showBackground = true, locale = "en")
@Composable
fun ShowCompressionPopupDialogPreview() {
    MediaKitTheme {
        ShowCompressionPopupDialog()
    }
}