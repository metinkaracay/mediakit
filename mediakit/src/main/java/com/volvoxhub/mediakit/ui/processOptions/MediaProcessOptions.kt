package com.volvoxhub.mediakit.ui.processOptions

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.volvoxhub.mediakit.R
import com.volvoxhub.mediakit.config.MediaEnums
import com.volvoxhub.mediakit.config.MediaHelper
import com.volvoxhub.mediakit.ui.alerts.PermissionAlertDialog
import com.volvoxhub.mediakit.ui.alerts.ShowCompressionPopupDialog
import com.volvoxhub.mediakit.ui.theme.MediaKitTheme
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.util.FileUtils
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaOptionsBottomSheet(
    options: () -> List<MediaEnums.PermissionOptionsEnum>,
    galleryType: MediaEnums.MediaTypeEnum,
    onDismiss: () -> Unit,
    compressedImageUri: (Uri?) -> Unit,
    videoCompressionLevel: MediaEnums.CompressVideoEnum,
    imageCompressionLevel: MediaEnums.CompressImageEnum,
    imageCropSize: MediaEnums.CropAspectRatioEnum,
    originalVideoSize: (Long) -> Unit,
    compressedVideoSize: (Long) -> Unit,
    compressedVideoUri: (Uri?) -> Unit,
    cameraPermissionDenied: () -> Unit = {},
    useDefaultWarnings: Boolean = false
) {
    val context = LocalContext.current
    val deniedPermission by remember { mutableStateOf("") }
    var showPermissionAlertDialog by remember { mutableStateOf(false) }
    val permissions = options()
    var isOpenCameraClicked by remember { mutableStateOf(false) }
    var isOpenGalleryClicked by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        permissions.forEach { permissionOption ->
            ClickableText(
                text = AnnotatedString(permissionOption.getPermissionText(context)),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
                    .fillMaxWidth(),
                onClick = {
                    when (permissionOption.getPermissionText(context)) {
                        MediaEnums.PermissionOptionsEnum.CAMERA_PERMISSION.getPermissionText(context) -> {
                            isOpenCameraClicked = true
                        }

                        MediaEnums.PermissionOptionsEnum.GALLERY_PERMISSION.getPermissionText(
                            context
                        ) -> {
                            isOpenGalleryClicked = true
                        }
                    }
                }
            )
        }
    }

    if (isOpenCameraClicked) {
        ProcessCameraMedia(
            compressedImageUri = { uri ->
                compressedImageUri.invoke(uri)
            },
            imageCropSize = imageCropSize,
            imageCompressionLevel = imageCompressionLevel,
            useDefaultWarnings = useDefaultWarnings,
            cameraPermissionDenied = cameraPermissionDenied
        )
    }

    if (isOpenGalleryClicked) {
        HandleGallerySelection(
            galleryType = galleryType,
            compressedImageUri = { uri ->
                compressedImageUri.invoke(uri)
            },
            videoCompressionLevel = videoCompressionLevel,
            imageCompressionLevel = imageCompressionLevel,
            imageCropSize = imageCropSize,
            originalVideoSize = { size ->
                originalVideoSize.invoke(size)
            },
            compressedVideoSize = { size ->
                compressedVideoSize.invoke(size)
            },
            compressedVideoUri = { uri ->
                compressedVideoUri.invoke(uri)
            }
        )
    }

    if (showPermissionAlertDialog) {
        PermissionAlertDialog(
            permission = deniedPermission,
            onConfirm = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    val uri = Uri.fromParts("package", context.packageName, null)
                    data = uri
                }
                context.startActivity(intent)
                showPermissionAlertDialog = false
            },
            onDismiss = {
                showPermissionAlertDialog = false
            }
        )
    }
}


@Composable
fun ProcessCameraMedia(
    compressedImageUri: (Uri?) -> Unit,
    imageCropSize: MediaEnums.CropAspectRatioEnum,
    imageCompressionLevel: MediaEnums.CompressImageEnum,
    cameraPermissionDenied: () -> Unit = {},
    useDefaultWarnings: Boolean = false,
    photoCaptureFailed: () -> Unit = {},
    photoCroppingFailed: () -> Unit = {},
    notCapturedAnything: () -> Unit = {}
) {
    val context = LocalContext.current
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var deniedPermission by remember { mutableStateOf("") }
    var showPermissionAlertDialog by remember { mutableStateOf(false) }
    var isCameraOpened by remember { mutableStateOf(false) }

    val cropActivityResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val resultUri = UCrop.getOutput(result.data!!)
                if (resultUri != null) {

                    compressedImageUri(resultUri)
                    //TODO kütüphane düzelene kadar üst taraf ile alt tarafı değiştirdik
                    /*MediaHelper.compressImage(context, imageCompressionLevel, resultUri) { compressedUri ->
                        compressedUri?.let {
                            capturedImageUri = compressedUri
                            compressedImageUri(compressedUri)
                        }
                    }*/
                } else {
                    if (!useDefaultWarnings) {
                        photoCroppingFailed()
                    } else {
                        Toast.makeText(
                            context,
                            context.getString(R.string.crop_failed_message),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                notCapturedAnything()
            }
            isCameraOpened = false
        }
    )

    val cameraContentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success && capturedImageUri != null) {
                MediaHelper.startImageCrop(
                    capturedImageUri!!,
                    context,
                    imageCropSize,
                    cropActivityResultLauncher
                )
            } else {
                if (!success) {
                    notCapturedAnything()
                }
                if (!useDefaultWarnings) {
                    photoCaptureFailed()
                } else {
                    Toast.makeText(context, R.string.photo_capture_failed, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    )

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted && !isCameraOpened) {
                isCameraOpened = true
                MediaHelper.openCamera(
                    context,
                    cameraContentLauncher,
                    { uri -> capturedImageUri = uri })
            } else {
                deniedPermission = context.getString(R.string.option_camera)
                showPermissionAlertDialog = true
            }
        }
    )

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (!isCameraOpened) {
                isCameraOpened = true
                MediaHelper.openCamera(context, cameraContentLauncher) { uri ->
                    capturedImageUri = uri
                }
            }
        } else {
            cameraPermissionDenied()
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (showPermissionAlertDialog && useDefaultWarnings) {
        PermissionAlertDialog(
            permission = deniedPermission,
            onConfirm = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    val uri = Uri.fromParts("package", context.packageName, null)
                    data = uri
                }
                context.startActivity(intent)
                showPermissionAlertDialog = false
            },
            onDismiss = {
                showPermissionAlertDialog = false
            }
        )
    }
}

@Composable
fun HandleGallerySelection(
    galleryType: MediaEnums.MediaTypeEnum,
    originalVideoSize: (Long) -> Unit,
    compressedVideoSize: (Long) -> Unit,
    compressedVideoUri: (Uri?) -> Unit,
    compressedImageUri: (Uri?) -> Unit,
    videoCompressionLevel: MediaEnums.CompressVideoEnum,
    imageCompressionLevel: MediaEnums.CompressImageEnum,
    imageCropSize: MediaEnums.CropAspectRatioEnum,
    useDefaultWarnings: Boolean = false,
    photoCroppingFailed: () -> Unit = {},
    notCapturedAnything: () -> Unit = {}
) {
    val context = LocalContext.current
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    val deniedPermission by remember { mutableStateOf("") }
    var showPermissionAlertDialog by remember { mutableStateOf(false) }
    var isCompressing by remember { mutableStateOf(false) }

    val cropActivityResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val resultUri = UCrop.getOutput(result.data!!)
                if (resultUri != null) {

                    compressedImageUri(resultUri)
                    //TODO kütüphane düzelene kadar üst taraf ile alt tarafı değiştirdik
                    /*MediaHelper.compressImage(context, imageCompressionLevel, resultUri) { compressedUri ->
                        compressedUri?.let {
                            capturedImageUri = compressedUri
                            compressedImageUri(compressedUri)
                        }
                    }*/
                } else {
                    if (!useDefaultWarnings) {
                        photoCroppingFailed()
                    } else {
                        Toast.makeText(
                            context,
                            context.getString(R.string.crop_failed_message),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                notCapturedAnything()
            }
        }
    )

    val galleryContentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            val uri = result.data?.data
            if (uri != null) {
                val mimeType = context.contentResolver.getType(uri)
                if (mimeType != null) {
                    if (mimeType.startsWith("image/")) {
                        MediaHelper.startImageCrop(
                            uri,
                            context,
                            imageCropSize,
                            cropActivityResultLauncher
                        )
                    } else if (mimeType.startsWith("video/")) {
                        val originalFilePath = FileUtils.getPath(context, uri)
                        val originalFile = File(originalFilePath!!)
                        originalVideoSize(originalFile.length())

                        //isCompressing = true

                        compressedVideoUri(uri)
                        val compressedFile = File(uri.path!!)
                        compressedVideoSize(compressedFile.length())
                        //TODO kütüphane düzelene kadar üst taraf ile alt tarafı değiştirdik
                        /*MediaHelper.compressVideo(context, videoCompressionLevel, uri) { compressedUri ->
                            compressedVideoUri(compressedUri)
                            compressedUri?.let {
                                val compressedFile = File(it.path!!)
                                compressedVideoSize(compressedFile.length())
                            }
                            isCompressing = false
                        }*/
                    } else {
                        if (useDefaultWarnings) Toast.makeText(
                            context,
                            R.string.unsupported_media_type,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    if (useDefaultWarnings) Toast.makeText(
                        context,
                        R.string.media_type_unknown,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                notCapturedAnything()
            }
        }
    )

    LaunchedEffect(Unit) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = galleryType.getMediaType()
            putExtra(Intent.EXTRA_MIME_TYPES, galleryType.getMimeTypes())
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        galleryContentLauncher.launch(intent)
    }

    if (showPermissionAlertDialog) {
        PermissionAlertDialog(
            permission = deniedPermission,
            onConfirm = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    val uri = Uri.fromParts("package", context.packageName, null)
                    data = uri
                }
                context.startActivity(intent)
                showPermissionAlertDialog = false
            },
            onDismiss = {
                showPermissionAlertDialog = false
            }
        )
    }

    if (isCompressing) {
        ShowCompressionPopupDialog()
    }
}

@Preview(showBackground = true)
@Composable
fun PermissionBottomSheetPreview() {
    MediaKitTheme {
        MediaOptionsBottomSheet(
            options = {
                listOf(
                    MediaEnums.PermissionOptionsEnum.CAMERA_PERMISSION,
                    MediaEnums.PermissionOptionsEnum.GALLERY_PERMISSION
                )
            },
            galleryType = MediaEnums.MediaTypeEnum.BOTH,
            onDismiss = { },
            compressedImageUri = { },
            videoCompressionLevel = MediaEnums.CompressVideoEnum.MEDIUM,
            imageCompressionLevel = MediaEnums.CompressImageEnum.MEDIUM,
            imageCropSize = MediaEnums.CropAspectRatioEnum.ORIGINAL,
            originalVideoSize = { },
            compressedVideoSize = { },
            compressedVideoUri = { }
        )
    }
}