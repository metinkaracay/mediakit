package com.volvoxhub.mediakit.config

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.FileProvider
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.util.FileUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

object MediaHelper {

    fun openCamera(
        context: Context,
        cameraContentLauncher: ActivityResultLauncher<Uri>,
        onUriCreated: (Uri) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val photoFile = File.createTempFile("photo_", ".jpg", context.cacheDir)
            val capturedImageUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            withContext(Dispatchers.Main) {
                onUriCreated(capturedImageUri)
                cameraContentLauncher.launch(capturedImageUri)
            }
        }
    }

    fun startImageCrop(
        sourceUri: Uri,
        context: Context,
        cropEnum: MediaEnums.CropAspectRatioEnum,
        cropActivityResultLauncher: ActivityResultLauncher<Intent>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val destinationUri = Uri.fromFile(
                File(context.cacheDir, "cropped_image_${System.currentTimeMillis()}.jpg")
            )
            val uCropIntent = UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(cropEnum.getAspectSize().first, cropEnum.getAspectSize().second)
                .getIntent(context)
            withContext(Dispatchers.Main) {
                cropActivityResultLauncher.launch(uCropIntent)
            }
        }
    }

    fun compressVideo(
        context: Context,
        videoCompressLevel: MediaEnums.CompressVideoEnum,
        sourceUri: Uri,
        onComplete: (Uri?) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val sourcePath = FileUtils.getPath(context, sourceUri)
            val destinationFile = File(context.cacheDir, "compressed_video_${System.currentTimeMillis()}.mp4")
            val destinationPath = destinationFile.absolutePath

            val command = videoCompressLevel.getCompressCommand(sourcePath!!, destinationPath)

            /*FFmpeg.executeAsync(command) { _, returnCode ->
                CoroutineScope(Dispatchers.Main).launch {
                    if (returnCode == Config.RETURN_CODE_SUCCESS) {
                        val compressedUri = Uri.fromFile(destinationFile)
                        onComplete(compressedUri)
                    } else {
                        onComplete(null)
                    }
                }
            }*/
        }
    }

    fun compressImage(
        context: Context,
        imageCompressLevel: MediaEnums.CompressImageEnum,
        inputUri: Uri,
        onComplete: (Uri?) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val inputPath = FileUtils.getPath(context, inputUri)
            val outputFile = File(context.cacheDir, "compressed_image_${System.currentTimeMillis()}.jpg")
            val outputPath = outputFile.absolutePath

            val command = imageCompressLevel.getCompressCommand(inputPath!!, outputPath)

            /*FFmpeg.executeAsync(command) { _, returnCode ->
                CoroutineScope(Dispatchers.Main).launch {
                    if (returnCode == Config.RETURN_CODE_SUCCESS) {
                        val compressedUri = Uri.fromFile(outputFile)
                        onComplete(compressedUri)
                    } else {
                        onComplete(null)
                    }
                }
            }*/
        }
    }
}
