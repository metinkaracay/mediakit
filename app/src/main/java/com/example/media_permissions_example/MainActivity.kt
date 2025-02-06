package com.example.media_permissions_example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import android.net.Uri
import android.util.Log
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.rememberAsyncImagePainter
import com.volvoxhub.mediakit.MediaKit
import com.volvoxhub.mediakit.config.MediaEnums

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            PermissionBottomSheetExample()
        }
    }
}

@Composable
fun PermissionBottomSheetExample() {
    val viewModel = viewModel<MainViewModel>()
    var isBottomSheetVisible by remember { mutableStateOf(false) }
    var isOpenCameraClicked by remember { mutableStateOf(false) }
    var isOpenGalleryClicked by remember { mutableStateOf(false) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var originalVideoSize by remember { mutableStateOf(0L) }
    var compressedVideoSize by remember { mutableStateOf(0L) }
    var compressedVideoUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .height(IntrinsicSize.Max),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Camera",
                            modifier = Modifier.size(56.dp)
                                .clickable {
                                    isOpenCameraClicked = true
                                }
                        )
                        Spacer(modifier = Modifier.width(60.dp))
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Gallery",
                            modifier = Modifier.size(56.dp)
                                .clickable {
                                    isOpenGalleryClicked = true
                                }
                        )
                    }
                    Button(onClick = {
                        isBottomSheetVisible = true
                    }) {
                        Text(text = stringResource(id = R.string.choose_media_button_text))
                    }

                    capturedImageUri?.let { uri ->
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = "Selected Image",
                            modifier = Modifier
                                .size(200.dp)
                                .padding(16.dp)
                        )
                    }

                    compressedVideoUri?.let {
                        VideoPlayerScreen(compressedVideoUri = it)
                    }

                    Text(text = "Original Video Size: ${viewModel.formatFileSize(originalVideoSize)}")
                    compressedVideoUri?.let {
                        Text(text = "Compressed Video Size: ${viewModel.formatFileSize(compressedVideoSize)}")
                    }
                }

                if (isOpenCameraClicked) {
                    MediaKit.OpenCamera(
                        compressedImageUri = { uri ->
                            capturedImageUri = uri
                        },
                        imageCropSize = MediaEnums.CropAspectRatioEnum.ASPECT_16_9,
                        notCapturedAnything = {

                        }
                    )
                }

                if (isOpenGalleryClicked) {
                    MediaKit.OpenGallery(
                        compressedImageUri = { uri ->
                            capturedImageUri = uri
                        },
                        imageCropSize = MediaEnums.CropAspectRatioEnum.ASPECT_16_9,
                        compressedVideoUri = { uri ->
                            if (uri != null) {
                                isBottomSheetVisible = false
                                compressedVideoUri = uri
                            }
                        },
                        notCapturedAnything = {

                        }
                    )
                }

                if (isBottomSheetVisible) {
                    MediaKit.ShowBottomSheet(
                        options = {
                            listOf(
                                MediaEnums.PermissionOptionsEnum.CAMERA_PERMISSION,
                                MediaEnums.PermissionOptionsEnum.GALLERY_PERMISSION
                            )
                        },
                        galleryType = MediaEnums.MediaTypeEnum.BOTH,
                        onDismiss = {
                            isBottomSheetVisible = false
                        },
                        compressedImageUri = { uri ->
                            capturedImageUri = uri
                        },
                        imageCropSize = MediaEnums.CropAspectRatioEnum.ASPECT_16_9,
                        originalVideoSize = { size -> originalVideoSize = size },
                        compressedVideoSize = { size -> compressedVideoSize = size },
                        compressedVideoUri = { uri ->
                            if (uri != null) {
                                isBottomSheetVisible = false
                                compressedVideoUri = uri
                            }
                        },
                        cameraPermissionDenied = {
                            Toast.makeText(context, "Camera permission denied test", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    )
}

@Composable
fun VideoPlayerScreen(compressedVideoUri: Uri?) {
    AndroidView(
        factory = { context ->
            VideoView(context).apply {
                setMediaController(MediaController(context))
                compressedVideoUri?.let {
                    setVideoURI(it)
                    requestFocus()
                    start()
                }
            }
        },
        update = { videoView ->
            compressedVideoUri?.let {
                videoView.setVideoURI(it)
                videoView.start()
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewPermissionBottomSheetExample() {
    PermissionBottomSheetExample()
}