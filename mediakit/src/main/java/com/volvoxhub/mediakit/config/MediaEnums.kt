package com.volvoxhub.mediakit.config

import android.content.Context
import com.volvoxhub.mediakit.R

object MediaEnums {
    enum class CompressVideoEnum {
        LOW, MEDIUM, HIGH;

        fun getCompressCommand(sourcePath: String, destinationPath: String): String {
            return when (this) {
                LOW -> "-i $sourcePath -c:v libx264 -crf 25 -preset ultrafast $destinationPath"
                MEDIUM -> "-i $sourcePath -c:v libx264 -crf 30 -preset ultrafast $destinationPath"
                HIGH -> "-i $sourcePath -c:v libx264 -crf 28 -preset faster $destinationPath"
            }
        }
    }

    enum class CompressImageEnum {
        LOW, MEDIUM, HIGH;

        fun getCompressCommand(sourcePath: String, destinationPath: String): String {
            return when (this) {
                LOW -> "-i $sourcePath -q:v 3 $destinationPath"
                MEDIUM -> "-i $sourcePath -q:v 5 $destinationPath"
                HIGH -> "-i $sourcePath -q:v 8 $destinationPath"
            }
        }
    }

    enum class PermissionOptionsEnum {
        CAMERA_PERMISSION, GALLERY_PERMISSION;

        fun getPermissionText(context: Context): String {
            return when (this) {
                CAMERA_PERMISSION -> context.getString(R.string.option_camera)
                GALLERY_PERMISSION -> context.getString(R.string.option_gallery)
            }
        }
    }

    enum class MediaTypeEnum {
        PHOTO, VIDEO, BOTH;

        fun getMediaType(): String {
            return when (this) {
                PHOTO -> "image/*"
                VIDEO -> "video/*"
                BOTH -> "*/*"
            }
        }

        fun getMimeTypes(): Array<String> {
            return when (this) {
                PHOTO -> arrayOf("image/*")
                VIDEO -> arrayOf("video/*")
                BOTH -> arrayOf("image/*", "video/*")
            }
        }
    }

    enum class CropAspectRatioEnum {
        ASPECT_1_1, ASPECT_3_4, ASPECT_3_2, ASPECT_16_9, ORIGINAL;

        fun getAspectSize(): Pair<Float, Float> {
            return when (this) {
                ASPECT_1_1 -> Pair(1f, 1f)
                ASPECT_3_4 -> Pair(3f, 4f)
                ASPECT_3_2 -> Pair(3f, 2f)
                ASPECT_16_9 -> Pair(16f, 9f)
                ORIGINAL -> Pair(0f, 0f)
            }
        }
    }
}