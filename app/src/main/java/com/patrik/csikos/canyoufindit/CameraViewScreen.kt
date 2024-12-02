package com.patrik.csikos.canyoufindit

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraViewScreen(modifier: Modifier = Modifier) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val snackbarHostState = remember { SnackbarHostState() }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted
        } else {
            // Handle permission denial
        }
    }

    LaunchedEffect(cameraPermissionState) {
        if (!cameraPermissionState.status.isGranted && cameraPermissionState.status.shouldShowRationale) {
            // Show rationale if needed
            snackbarHostState.showSnackbar("Please give access to the camera to scan the QR code.")
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }
    ) { innerPadding ->
        Column(
            modifier = modifier.padding(innerPadding).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text("Search for 'Hello World' text", style = MaterialTheme.typography.headlineMedium)

            var textMessage by remember { mutableStateOf("") }
            if (cameraPermissionState.status.isGranted) {
                // Obtain the current context and lifecycle owner
                val context = LocalContext.current
                val lifecycleOwner = LocalLifecycleOwner.current

                // Remember a LifecycleCameraController for this composable
                val cameraController = remember {
                    LifecycleCameraController(context).apply {
                        // Bind the LifecycleCameraController to the lifecycleOwner
                        bindToLifecycle(lifecycleOwner)
                        // Set the camera selector to the back camera
                        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        setImageAnalysisAnalyzer(
                            Executors.newSingleThreadExecutor(),
                            YourImageAnalyzer(
                                onSuccess = { text ->
                                    // Handle the successful text recognition result
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=dQw4w9WgXcQ/"))
                                    if(text.text.contains("Hello World", ignoreCase = true)) {
                                        context.startActivity(intent)
                                    }
                                    else {
                                        textMessage = text.text
                                    }
                                },
                                onFailure = { e ->
                                    // Handle the text recognition failure

                                }
                            )
                        )
                    }
                }

                Text("Scanned text:", style = MaterialTheme.typography.bodyLarge)
                Text(textMessage, style = MaterialTheme.typography.bodyLarge)

                // Key Point: Displaying the Camera Preview
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        // Initialize the PreviewView and configure it
                        PreviewView(ctx).apply {
                            scaleType = PreviewView.ScaleType.FILL_START
                            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                            controller = cameraController // Set the controller to manage the camera lifecycle
                        }
                    },
                    onRelease = {
                        // Release the camera controller when the composable is removed from the screen
                        cameraController.unbind()
                    }
                )
            }
            else {
                Text("Can't open the camera, because you disabled the camera permission.")
            }
        }
    }
}