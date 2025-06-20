package com.example.mycamera.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import com.example.mycamera.data.User
import com.example.mycamera.ui.components.BottomNavigationBar
import com.example.mycamera.ui.components.NavigationDrawer
import com.example.mycamera.ui.components.TopAppBar
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePage(navController: NavController) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val auth: FirebaseAuth = Firebase.auth
    val db: FirebaseFirestore = Firebase.firestore
    val currentUserId = auth.currentUser?.uid

    var userProfile by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
            errorMessage = "No user logged in."
            isLoading = false
            Log.d("ProfilePage", "No current user ID to fetch profile.")
            return
        }
        try {
            val userDoc = db.collection("users").document(currentUserId).get().await()
            if (userDoc.exists()) {
                val fetchedUsername = userDoc.getString("username") ?: "Unknown User"
                val fetchedEmail = userDoc.getString("email") ?: ""
                val fetchedProfilePicUrl = userDoc.getString("profilePictureUrl") ?: ""
                val fetchedFollowers = userDoc.get("followers") as? List<String> ?: emptyList()
                val fetchedFollowing = userDoc.get("following") as? List<String> ?: emptyList()

                userProfile = User(
                    id = userDoc.id,
                    username = fetchedUsername,
                    profilePictureUrl = fetchedProfilePicUrl.ifEmpty { "https://placehold.co/80x80/87CEEB/FFFFFF?text=${fetchedUsername.firstOrNull()?.toUpperCase() ?: '?'}" },
                    followers = fetchedFollowers,
                    following = fetchedFollowing
                )
                Log.d("ProfilePage", "Fetched user profile: ${userProfile?.username}, Following: ${userProfile?.following?.size}, Followers: ${userProfile?.followers?.size}")
            } else {
                userProfile = null
                errorMessage = "User profile not found in Firestore."
                Log.d("ProfilePage", "User profile document does not exist for ID: $currentUserId")
            }
        } catch (e: Exception) {
            Log.e("ProfilePage", "Error fetching user profile: ${e.message}", e)
            errorMessage = "Failed to load profile: ${e.message}"
            userProfile = null
        } finally {
            isLoading = false
        }
    }


    LaunchedEffect(currentUserId) {
        fetchUserProfile()
    }

    val userPosts = remember { mutableStateListOf<FeedItem>() }
    LaunchedEffect(Unit) {
        userPosts.addAll(
            listOf(
                FeedItem(
                    id = "post1",
                    username = "John Smith",
                    profilePictureUrl = "https://placehold.co/40x40/87CEEB/FFFFFF?text=JS",
                    imageUrl = "https://placehold.co/200x200/B88C4A/FFFFFF?text=My+Post+1",
                    location = "Unknown",
                    date = "Jan 1, 2026",
                    description = "My first post!",
                    likesCount = 50,
                    comments = emptyList()
                ),
                FeedItem(
                    id = "post2",
                    username = "John Smith",
                    profilePictureUrl = "https://placehold.co/40x40/87CEEB/FFFFFF?text=JS",
                    imageUrl = "https://placehold.co/200x200/8B4513/FFFFFF?text=My+Post+2",
                    location = "Unknown",
                    date = "Jan 5, 2026",
                    description = "Another great day!",
                    likesCount = 75,
                    comments = emptyList()
                ),
                FeedItem(
                    id = "post3",
                    username = "John Smith",
                    profilePictureUrl = "https://placehold.co/40x40/87CEEB/FFFFFF?text=JS",
                    imageUrl = "https://placehold.co/200x200/3CB371/FFFFFF?text=My+Post+3",
                    location = "Unknown",
                    date = "Jan 10, 2026",
                    description = "Loving this view.",
                    likesCount = 90,
                    comments = emptyList()
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
                    titleString = "Profile"
                )
            },
            bottomBar = {
                BottomNavigationBar(
                    navController = navController
                )
            },
            containerColor = Color(0xFFF5F0E7)
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "An unknown error occurred.",
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        color = Color.Red
                    )
                } else if (userProfile != null) {
                    ProfileContent(user = userProfile!!, posts = userPosts)
                } else {
                    Text(
                        text = "No profile data available. Please log in.",
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileContent(user: User, posts: List<FeedItem>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = user.profilePictureUrl.ifEmpty { "https://placehold.co/80x80/87CEEB/FFFFFF?text=PIC" },
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.Gray),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(user.username, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFF4C4C4C))

                val friendCount = user.following.size
                val friendText = if (friendCount == 1) "$friendCount friend" else "$friendCount friends"

                Text(friendText, fontSize = 14.sp, color = Color.Gray)
            }
        }

        Divider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(4.dp)
        ) {
            items(posts.chunked(3)) { rowItems ->
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
                                .clickable {  }
                        ) {
                            AsyncImage(
                                model = item.imageUrl,
                                contentDescription = "User Post",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Post",
                                tint = Color.White,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(24.dp)
                                    .clickable { /* TODO: Implement delete post */ }
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
