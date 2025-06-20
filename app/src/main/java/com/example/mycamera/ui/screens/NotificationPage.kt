package com.example.mycamera.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mycamera.R
import com.example.mycamera.data.NotificationItem
import com.example.mycamera.data.User
import com.example.mycamera.ui.components.BottomNavigationBar
import com.example.mycamera.ui.components.NavigationDrawer
import com.example.mycamera.ui.components.NotificationCard
import com.example.mycamera.ui.components.TopAppBar
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationPage(navController: NavController) {
    val notifications = remember { mutableStateListOf<NotificationItem>() }
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

    suspend fun fetchUserProfile() {
        if (currentUserId == null) {
            userProfile = null
            Log.d("NotificationPage", "No current user ID to fetch profile.")
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
                Log.d("NotificationPage", "Fetched user profile: ${userProfile?.username}")
            } else {
                userProfile = null
                Log.d("NotificationPage", "User profile document does not exist for ID: $currentUserId")
            }
        } catch (e: Exception) {
            Log.e("NotificationPage", "Error fetching user profile: ${e.message}", e)
            userProfile = null
        }
    }

    LaunchedEffect(currentUserId) {
        fetchUserProfile()
    }

    LaunchedEffect(Unit) {
        notifications.addAll(
            listOf(
                NotificationItem(
                    id = "n1",
                    username = "Rachel",
                    profilePictureUrl = "https://placehold.co/40x40/FF6347/FFFFFF?text=R",
                    type = "friend_request"
                ),
                NotificationItem(
                    id = "n2",
                    username = "Michael",
                    profilePictureUrl = "https://placehold.co/40x40/4682B4/FFFFFF?text=M",
                    type = "friend_request"
                ),
                NotificationItem(
                    id = "n3",
                    username = "Emma",
                    profilePictureUrl = "https://placehold.co/40x40/3CB371/FFFFFF?text=E",
                    type = "friend_request"
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
                    titleString = "Notification"
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notifications) { notification ->
                        NotificationCard(notificationItem = notification)
                    }
                }
            }
        }
    }
}
