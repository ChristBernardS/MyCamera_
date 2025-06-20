package com.example.mycamera.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.mycamera.R
import com.example.mycamera.data.FeedItem
import com.example.mycamera.data.User
import com.example.mycamera.ui.components.BottomNavigationBar
import com.example.mycamera.ui.components.FeedCard
import com.example.mycamera.ui.components.TopAppBar
import com.example.mycamera.ui.components.NavigationDrawer
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(navController: NavController) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val feedItems = remember { mutableStateListOf<FeedItem>() }
    var isLoadingFeed by remember { mutableStateOf(true) }
    var feedErrorMessage by remember { mutableStateOf<String?>(null) }
    var hasStoragePermission by remember { mutableStateOf(false) }

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

    suspend fun fetchUserProfile() {
        if (currentUserId == null) {
            userProfile = null
            Log.d("HomePage", "No current user ID to fetch profile.")
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
                Log.d("HomePage", "Fetched user profile: ${userProfile?.username}")
            } else {
                userProfile = null
                Log.d("HomePage", "User profile document does not exist for ID: $currentUserId")
            }
        } catch (e: Exception) {
            Log.e("HomePage", "Error fetching user profile: ${e.message}", e)
            userProfile = null
        }
    }


    val permissionToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasStoragePermission = isGranted
        if (!isGranted) {
            feedErrorMessage = "Storage permission denied. Cannot load local images."
            isLoadingFeed = false
            Log.d("HomePage", "Storage permission denied by user.")
        } else {
            feedErrorMessage = null
            Log.d("HomePage", "Storage permission granted by user.")
        }
    }

    suspend fun fetchLocalImages(context: Context, feedItemsList: MutableList<FeedItem>) {
        isLoadingFeed = true
        feedErrorMessage = null
        feedItemsList.clear()

        if (!hasStoragePermission) {
            feedErrorMessage = "Storage permission not granted. Please allow access to view local photos."
            isLoadingFeed = false
            Log.d("HomePage", "fetchLocalImages: Permission not held inside function.")
            return
        }

        withContext(Dispatchers.IO) {
            val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
            Log.d("HomePage", "Querying image collection: $imageCollection")

            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.DATA
            )

            val folderName = "MyCamera"
            val selection: String
            val selectionArgs: Array<String>

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                selection = "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?"
                selectionArgs = arrayOf("%${Environment.DIRECTORY_PICTURES}/$folderName/%")
            } else {
                selection = "${MediaStore.Images.Media.DATA} LIKE ?"
                selectionArgs = arrayOf("%${Environment.getExternalStorageDirectory()}${File.separator}${Environment.DIRECTORY_PICTURES}${File.separator}${folderName}${File.separator}%")
            }
            Log.d("HomePage", "MediaStore Query: Selection = '$selection', Args = '${selectionArgs.contentToString()}'")


            val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

            try {
                context.contentResolver.query(
                    imageCollection,
                    projection,
                    selection,
                    selectionArgs,
                    sortOrder
                )?.use { cursor ->
                    Log.d("HomePage", "Cursor obtained. Row count: ${cursor.count}")
                    if (cursor.count == 0) {
                        Log.d("HomePage", "No images found with current query criteria in $folderName folder.")
                    }

                    val idColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                    val displayNameColumn = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
                    val dateAddedColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED)
                    val pathColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA)

                    if (idColumn == -1 || displayNameColumn == -1 || dateAddedColumn == -1) {
                        Log.e("HomePage", "One or more required MediaStore columns not found. _ID: $idColumn, DISPLAY_NAME: $displayNameColumn, DATE_ADDED: $dateAddedColumn")
                        feedErrorMessage = "Error: MediaStore columns missing."
                        return@withContext
                    }

                    var imagesFound = 0
                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idColumn)
                        val displayName = cursor.getString(displayNameColumn)
                        val dateAdded = cursor.getLong(dateAddedColumn)
                        val imageUri: Uri = Uri.withAppendedPath(imageCollection, id.toString())
                        val imagePath = if (pathColumn != -1) cursor.getString(pathColumn) else "N/A (Path column not available or not used)"

                        Log.d("HomePage", "Processing image: $displayName, URI: $imageUri, Path: $imagePath")

                        val formattedDate = SimpleDateFormat("MMM dd,yyyy", Locale.US).format(Date(dateAdded * 1000))

                        feedItemsList.add(
                            FeedItem(
                                id = imageUri.toString(),
                                username = "You",
                                profilePictureUrl = "https://placehold.co/40x40/000000/FFFFFF?text=ME",
                                imageUrl = imageUri,
                                location = "Local Photo",
                                date = formattedDate,
                                description = "Captured with MyCamera: $displayName",
                                likesCount = 0,
                                comments = emptyList(),
                                isLiked = false
                            )
                        )
                        imagesFound++
                    }
                    Log.d("HomePage", "Total images added from local storage: $imagesFound")
                }

                if (feedItemsList.isEmpty()) {
                    feedItemsList.addAll(
                        listOf(
                            FeedItem(
                                id = "local_dummy_1",
                                username = "Local Dummy 1",
                                profilePictureUrl = "https://placehold.co/40x40/0000FF/FFFFFF?text=D1",
                                imageUrl = R.drawable.dummy1,
                                location = "App Resources",
                                date = SimpleDateFormat("MMM dd,yyyy", Locale.US).format(Date()),
                                description = "Placeholder from app resources.",
                                likesCount = 10,
                                comments = emptyList(),
                                isLiked = false
                            ),
                            FeedItem(
                                id = "local_dummy_2",
                                username = "Local Dummy 2",
                                profilePictureUrl = "https://placehold.co/40x40/FF5733/FFFFFF?text=D2",
                                imageUrl = R.drawable.dummy2,
                                location = "App Resources",
                                date = SimpleDateFormat("MMM dd,yyyy", Locale.US).format(Date()),
                                description = "Another placeholder.",
                                likesCount = 5,
                                comments = emptyList(),
                                isLiked = false
                            )
                        )
                    )
                    feedErrorMessage = "No photos found in 'MyCamera' folder. Showing dummy resources."
                    Log.d("HomePage", "No local photos found, displaying dummy resources.")
                } else {
                    feedErrorMessage = null
                    Log.d("HomePage", "Successfully loaded ${feedItemsList.size} local photos.")
                }
            } catch (e: Exception) {
                Log.e("HomePage", "Error fetching local images: ${e.message}", e)
                feedErrorMessage = "Failed to load local images: ${e.message}"
            } finally {
                isLoadingFeed = false
            }
        }
    }

    LaunchedEffect(currentUserId) {
        fetchUserProfile()
    }

    LaunchedEffect(Unit) {
        val permissionStatus = ContextCompat.checkSelfPermission(context, permissionToRequest)
        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            hasStoragePermission = true
            scope.launch { fetchLocalImages(context, feedItems) }
        } else {
            requestPermissionLauncher.launch(permissionToRequest)
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
                    titleString = "Home",
                    drawerState = drawerState,
                    scope = scope,
                    navController = navController
                )
            },
            bottomBar = {
                BottomNavigationBar(
                    navController = navController
                )
            },
            containerColor = Color(0xFFF5F0E7)
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                OutlinedTextField(
                    value = "",
                    onValueChange = { /* TODO */ },
                    label = { Text("Search...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .background(Color.White, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                when {
                    isLoadingFeed -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        Text("Loading local photos...", modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                    feedErrorMessage != null -> {
                        Text(
                            text = "Error: $feedErrorMessage",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                    else -> {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(450.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            items(feedItems) { feed ->
                                FeedCard(
                                    feedItem = feed,
                                    modifier = Modifier.width(300.dp),
                                    onLikeClick = { postId, newIsLiked ->
                                        val index = feedItems.indexOfFirst { it.id == postId }
                                        if (index != -1) {
                                            feedItems[index] = feedItems[index].copy(
                                                likesCount = if (newIsLiked) feedItems[index].likesCount + 1 else feedItems[index].likesCount - 1,
                                                isLiked = newIsLiked
                                            )
                                            Log.d("HomePage", "Post $postId liked status updated to $newIsLiked. New likes: ${feedItems[index].likesCount}")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}