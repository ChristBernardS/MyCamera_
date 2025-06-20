// CameraPage.kt
package com.example.mycamera.ui.screens

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.mycamera.R
import com.example.mycamera.data.FeedItem
import com.example.mycamera.data.User
import com.example.mycamera.ui.components.BottomNavigationBar
import com.example.mycamera.ui.components.NavigationDrawer
import com.example.mycamera.ui.components.TopAppBar
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private var previewViewInstance: PreviewView? = null

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraPage(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val auth: FirebaseAuth = Firebase.auth
    val db: FirebaseFirestore = Firebase.firestore
    val currentUserId = auth.currentUser?.uid

    var userProfile by remember { mutableStateOf<User?>(null) }

    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    var hasCameraPermission by remember { mutableStateOf(false) }
    var currentLensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    val imageCapture: MutableState<ImageCapture?> = remember { mutableStateOf(null) }
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    var message by remember { mutableStateOf("") }

    var isCapturing by remember { mutableStateOf(false) }

    suspend fun fetchUserProfile() {
        if (currentUserId == null) {
            userProfile = null
            Log.d("CameraPage", "No current user ID to fetch profile.")
            return
        }
        try {
            val userDoc = db.collection("users").document(currentUserId).get().await()
            if (userDoc.exists()) {
                val fetchedUsername = userDoc.getString("username") ?: "Unknown User"
                val fetchedEmail = userDoc.getString("email") ?: ""
                val fetchedProfilePicUrl = userDoc.getString("profilePictureUrl") ?: ""

                userProfile = User(
                    id = userDoc.id,
                    username = fetchedUsername,
                    profilePictureUrl = fetchedProfilePicUrl.ifEmpty { "https://placehold.co/60x60/87CEEB/FFFFFF?text=${fetchedUsername.firstOrNull()?.toUpperCase() ?: '?'}" }
                )
                Log.d("CameraPage", "Fetched user profile: ${userProfile?.username}")
            } else {
                userProfile = null
                Log.d("CameraPage", "User profile document does not exist for ID: $currentUserId")
            }
        } catch (e: Exception) {
            Log.e("CameraPage", "Error fetching user profile: ${e.message}", e)
            userProfile = null
        }
    }


    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasCameraPermission = isGranted
        if (isGranted) {
            message = "Camera permission granted. Starting camera..."
        } else {
            message = "Camera permission denied. Cannot use camera."
        }
    }

    LaunchedEffect(Unit) {
        fetchUserProfile()

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            hasCameraPermission = true
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(hasCameraPermission, currentLensFacing, previewViewInstance) {
        val currentPreviewView = previewViewInstance
        if (hasCameraPermission && currentPreviewView != null) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(currentPreviewView.surfaceProvider)
                    }

                imageCapture.value = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(currentLensFacing)
                    .build()

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture.value
                    )
                    Log.d("CameraPage", "Camera bound with lens: ${currentLensFacing}")
                    message = "Camera ready: ${if (currentLensFacing == CameraSelector.LENS_FACING_BACK) "Back" else "Front"} camera"
                } catch (exc: Exception) {
                    Log.e("CameraPage", "Use case binding failed", exc)
                    message = "Camera error: ${exc.message}"
                }
            }, ContextCompat.getMainExecutor(context))
        }
    }

    NavigationDrawer(
        drawerState = drawerState,
        scope = scope,
        navController = navController,
        auth = auth,
        googleSignInClient = googleSignInClient,
        userProfile = userProfile
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    drawerState = drawerState,
                    scope = scope,
                    navController = navController,
                    titleString = "Camera"
                )
            },
            bottomBar = {
                BottomNavigationBar(
                    navController = navController
                )
            },
            containerColor = Color(0xFF4C4C4C)
        ) { paddingValues ->             Box(
                modifier = Modifier.fillMaxSize()
            ) {
                if (hasCameraPermission) {
                    AndroidView(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(0.dp))
                            .background(Color.Black),
                        factory = { ctx ->
                            PreviewView(ctx).apply {
                                this.scaleType = PreviewView.ScaleType.FILL_CENTER
                                previewViewInstance = this
                            }
                        },
                        update = { }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Red.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Camera Access Required", color = Color.White, fontSize = 20.sp)
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Spacer(modifier = Modifier.weight(1f))

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            onClick = {
                                if (hasCameraPermission && imageCapture.value != null && !isCapturing) {
                                    isCapturing = true
                                    takePhoto(context, imageCapture.value!!, cameraExecutor) { uri ->
                                        scope.launch(Dispatchers.Main) {
                                            isCapturing = false
                                            if (uri != Uri.EMPTY) {
                                                message = "Photo captured: $uri"
                                                Log.d("CameraPage", "Photo captured: $uri")
                                                navController.navigate("home") {
                                                    popUpTo("camera") { inclusive = true }
                                                }
                                            } else {
                                                message = "Failed to capture photo."
                                            }
                                        }
                                    }
                                } else if (!hasCameraPermission) {
                                    message = "Cannot capture: Camera permission not granted."
                                    requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                                } else if (isCapturing) {
                                    message = "Already capturing. Please wait."
                                } else {
                                    message = "Camera not ready. Please wait."
                                }
                            },
                            enabled = !isCapturing,
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            if (isCapturing) {
                                CircularProgressIndicator(color = Color(0xFFB88C4A), modifier = Modifier.size(40.dp))
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(32.dp))
                        IconButton(onClick = {
                            if (hasCameraPermission && !isCapturing) {
                                currentLensFacing = if (currentLensFacing == CameraSelector.LENS_FACING_BACK) {
                                    CameraSelector.LENS_FACING_FRONT
                                } else {
                                    CameraSelector.LENS_FACING_BACK
                                }
                                message = "Switching camera to ${if (currentLensFacing == CameraSelector.LENS_FACING_BACK) "back" else "front"}..."
                            } else if (isCapturing) {
                                message = "Cannot switch camera while capturing."
                            } else {
                                message = "Cannot switch camera: Permission not granted."
                            }
                        }, enabled = !isCapturing) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Switch Camera",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

private fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    cameraExecutor: ExecutorService,
    onPhotoCaptured: (Uri) -> Unit
) {
    val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + "MyCamera")
        }
    }

    val outputOptions = ImageCapture.OutputFileOptions.Builder(
        context.contentResolver,
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    ).build()

    imageCapture.takePicture(
        outputOptions,
        cameraExecutor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
                Log.e("CameraPage", "Photo capture failed: ${exc.message}", exc)
                onPhotoCaptured(Uri.EMPTY)
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                output.savedUri?.let { uri ->
                    onPhotoCaptured(uri)
                } ?: run {
                    Log.e("CameraPage", "Photo capture failed: Saved URI is null")
                    onPhotoCaptured(Uri.EMPTY)
                }
            }
        }
    )
}
