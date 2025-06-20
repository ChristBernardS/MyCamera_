// LikedPhotosPage.kt
package com.example.mycamera.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.mycamera.R
import com.example.mycamera.data.FeedItem
import com.example.mycamera.data.NotificationItem
import com.example.mycamera.data.User
import com.example.mycamera.ui.components.BottomNavigationBar
import com.example.mycamera.ui.components.NavigationDrawer
import com.example.mycamera.ui.components.NotificationCard
import com.example.mycamera.ui.components.TopAppBar
import com.example.mycamera.ui.components.NavigationDrawer
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LikedPhotosPage(navController: NavController) {
    val context = LocalContext.current
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

    val likedPhotos = remember { mutableStateListOf<FeedItem>() }

    suspend fun fetchUserProfile() {
        if (currentUserId == null) {
            userProfile = null
            Log.d("LikedPhotosPage", "No current user ID to fetch profile.")
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
                Log.d("LikedPhotosPage", "Fetched user profile: ${userProfile?.username}")
            } else {
                userProfile = null
                Log.d("LikedPhotosPage", "User profile document does not exist for ID: $currentUserId")
            }
        } catch (e: Exception) {
            Log.e("LikedPhotosPage", "Error fetching user profile: ${e.message}", e)
            userProfile = null
        }
    }

    LaunchedEffect(Unit) {
        fetchUserProfile()

        likedPhotos.addAll(
            listOf(
                FeedItem(
                    id = "liked1",
                    username = "John Smith",
                    profilePictureUrl = "https://placehold.co/40x40/87CEEB/FFFFFF?text=JS",
                    imageUrl = "https://placehold.co/200x200/B88C4A/FFFFFF?text=Liked+Photo+1",
                    location = "Bali",
                    date = "Mar 25, 2026",
                    description = "Magical island worth discovering",
                    likesCount = 1,
                    comments = emptyList(),
                    isLiked = true
                ),
                FeedItem(
                    id = "liked2",
                    username = "John Smith",
                    profilePictureUrl = "https://placehold.co/40x40/87CEEB/FFFFFF?text=JS",
                    imageUrl = "https://placehold.co/200x200/8B4513/FFFFFF?text=Liked+Photo+2",
                    location = "Paris",
                    date = "Feb 10, 2026",
                    description = "City of love",
                    likesCount = 1,
                    comments = emptyList(),
                    isLiked = true
                )
            )
        )
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
                    titleString = "Liked"
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
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    items(likedPhotos.chunked(3)) { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            rowItems.forEach { item ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.LightGray)
                                ) {
                                    AsyncImage(
                                        model = item.imageUrl,
                                        contentDescription = "Liked Photo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                            for (i in 0 until (3 - rowItems.size)) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}
