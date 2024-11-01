package com.volvoxhub.mediakit

import android.net.Uri
import androidx.compose.runtime.Composable
import com.volvoxhub.mediakit.config.MediaEnums
import com.volvoxhub.mediakit.ui.bottomDialog.MediaOptionsBottomSheet

class MediaKit {

    companion object {

        @Composable
        fun ShowBottomSheet(
            options: () -> List<MediaEnums.PermissionOptionsEnum>,
            galleryType: MediaEnums.MediaTypeEnum = MediaEnums.MediaTypeEnum.BOTH,
            onDismiss: () -> Unit,
            compressedImageUri: ((Uri?) -> Unit)? = null,
            imageCropSize: MediaEnums.CropAspectRatioEnum = MediaEnums.CropAspectRatioEnum.ORIGINAL,
            videoCompressionLevel: MediaEnums.CompressVideoEnum = MediaEnums.CompressVideoEnum.MEDIUM,
            imageCompressionLevel: MediaEnums.CompressImageEnum = MediaEnums.CompressImageEnum.MEDIUM,
            originalVideoSize: ((Long) -> Unit)? = null,
            compressedVideoSize: ((Long) -> Unit)? = null,
            compressedVideoUri: ((Uri?) -> Unit)? = null,
        ) {
            MediaOptionsBottomSheet(
                options = options,
                galleryType = galleryType,
                onDismiss = onDismiss,
                compressedImageUri = { uri ->
                    compressedImageUri?.invoke(uri)
                },
                videoCompressionLevel = videoCompressionLevel,
                imageCompressionLevel = imageCompressionLevel,
                imageCropSize = imageCropSize,
                originalVideoSize = { size ->
                    originalVideoSize?.invoke(size)
                },
                compressedVideoSize = { size ->
                    compressedVideoSize?.invoke(size)
                },
                compressedVideoUri = { uri ->
                    compressedVideoUri?.invoke(uri)
                }
            )
        }
    }
}