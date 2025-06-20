// SearchUsersPage.kt
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
import androidx.compose.material.icons.filled.Search
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
import com.example.mycamera.data.User // Impor User data class
import com.example.mycamera.ui.components.BottomNavigationBar // Impor BottomNavigationBar
import com.example.mycamera.ui.components.UserSearchCard // Impor UserSearchCard
import com.example.mycamera.ui.components.NavigationDrawer // Ganti NavigationDrawer dengan HomeDrawer
import com.example.mycamera.ui.components.TopAppBar // Ganti TopAppBar dengan HomeTopAppBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.android.gms.auth.api.signin.GoogleSignIn // Impor GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient // Impor GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions // Impor GoogleSignInOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchUsersPage(navController: NavController) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope() // Dapatkan CoroutineScope untuk launch coroutines

    val auth: FirebaseAuth = Firebase.auth
    val db: FirebaseFirestore = Firebase.firestore
    val currentUserId = auth.currentUser?.uid // Dapatkan ID pengguna saat ini

    var userProfile by remember { mutableStateOf<User?>(null) } // State untuk menyimpan data profil pengguna

    val gso = remember { // Configure Google Sign-In client for sign out
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    var searchQuery by remember { mutableStateOf("") }
    val searchResults = remember { mutableStateListOf<User>() }
    var message by remember { mutableStateOf("") } // Pesan untuk pengguna (contoh: "User added!")

    // State untuk menyimpan ID pengguna yang sudah diikuti oleh pengguna saat ini
    val followedUserIds = remember { mutableStateListOf<String>() }

    // Fungsi untuk mengambil data profil pengguna dari Firestore (untuk HomeDrawer)
    suspend fun fetchUserProfile() {
        if (currentUserId == null) {
            userProfile = null
            Log.d("SearchUsersPage", "No current user ID to fetch profile.")
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
                    profilePictureUrl = fetchedProfilePicUrl.ifEmpty { "https://placehold.co/60x60/87CEEB/FFFFFF?text=${fetchedUsername.firstOrNull()?.toUpperCase() ?: '?'}" },
                    followers = fetchedFollowers,
                    following = fetchedFollowing
                )
                Log.d("SearchUsersPage", "Fetched user profile: ${userProfile?.username}")
            } else {
                userProfile = null
                Log.d("SearchUsersPage", "User profile document does not exist for ID: $currentUserId")
            }
        } catch (e: Exception) {
            Log.e("SearchUsersPage", "Error fetching user profile: ${e.message}", e)
            userProfile = null
        }
    }

    // Fungsi untuk mengambil daftar pengguna yang diikuti oleh pengguna saat ini
    suspend fun fetchCurrentUserFollowing() {
        if (currentUserId == null) {
            message = "User not logged in."
            return // Sekarang return dari fungsi, bukan dari lambda
        }
        try {
            val userDoc = db.collection("users").document(currentUserId).get().await()
            val followingList = userDoc.get("following") as? List<String> ?: emptyList()
            followedUserIds.clear()
            followedUserIds.addAll(followingList)
        } catch (e: Exception) {
            Log.e("SearchUsersPage", "Error fetching following list: ${e.message}", e)
            message = "Failed to load following list: ${e.message}"
        }
    }

    // Ambil data profil dan daftar following saat komponen pertama kali dimuat
    LaunchedEffect(currentUserId) {
        fetchUserProfile() // Panggil fetchUserProfile di sini
        if (currentUserId != null) {
            fetchCurrentUserFollowing() // Panggil fungsi lokal
        }
    }

    // Logika pencarian pengguna
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            searchResults.clear()
            message = "Searching for '${searchQuery}'..."
            try {
                // Query Firestore untuk mencari pengguna berdasarkan username
                val querySnapshot = db.collection("users")
                    .whereGreaterThanOrEqualTo("username", searchQuery)
                    .whereLessThanOrEqualTo("username", searchQuery + "\uf8ff")
                    .get()
                    .await()

                val users = querySnapshot.documents.mapNotNull { doc ->
                    val userId = doc.id
                    val username = doc.getString("username")
                    // Filter keluar pengguna yang sedang login dan pengguna yang tidak memiliki username
                    if (userId != currentUserId && username != null) {
                        User(
                            id = userId,
                            username = username,
                            profilePictureUrl = "https://placehold.co/40x40/FFD700/FFFFFF?text=${username.firstOrNull()?.toUpperCase() ?: '?'}" // Placeholder gambar profil
                        )
                    } else {
                        null
                    }
                }
                searchResults.addAll(users)
                if (users.isEmpty()) {
                    message = "No users found for '${searchQuery}'."
                } else {
                    message = "" // Kosongkan pesan jika ada hasil
                }

            } catch (e: Exception) {
                Log.e("SearchUsersPage", "Error searching users: ${e.message}", e)
                message = "Error searching users: ${e.message}"
            }
        } else {
            searchResults.clear()
            message = "" // Kosongkan pesan saat kolom pencarian kosong
        }
    }

    // Fungsi untuk menambahkan teman (mengikuti pengguna lain)
    suspend fun addFriend(targetUserId: String, targetUsername: String) {
        if (currentUserId == null) {
            message = "Please log in to add friends."
            return
        }

        if (followedUserIds.contains(targetUserId)) {
            message = "You are already following ${targetUsername}."
            return
        }

        try {
            // 1. Tambahkan targetUserId ke array 'following' pengguna saat ini
            db.collection("users").document(currentUserId)
                .update("following", FieldValue.arrayUnion(targetUserId))
                .await()

            // 2. Tambahkan currentUserId ke array 'followers' pengguna target
            db.collection("users").document(targetUserId)
                .update("followers", FieldValue.arrayUnion(currentUserId))
                .await()

            followedUserIds.add(targetUserId) // Perbarui state lokal
            message = "You are now following ${targetUsername}!"
        } catch (e: Exception) {
            Log.e("SearchUsersPage", "Error adding friend: ${e.message}", e)
            message = "Failed to add ${targetUsername}: ${e.message}"
        }
    }

    NavigationDrawer( // Mengganti NavigationDrawer dengan HomeDrawer
        drawerState = drawerState,
        scope = scope,
        navController = navController,
        auth = auth, // Meneruskan instance auth
        googleSignInClient = googleSignInClient,
        userProfile = userProfile // <-- Meneruskan userProfile ke HomeDrawer
    ) {
        Scaffold(
            topBar = {
                TopAppBar( // Menggunakan komponen terpisah untuk TopAppBar
                    drawerState = drawerState,
                    scope = scope,
                    navController = navController,
                    titleString = "Add Friend" // Judul untuk halaman ini
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
                Text(
                    text = "Search your friend username here",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF4C4C4C),
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("username") },
                    trailingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    modifier = Modifier
                        .fillMaxWidth()
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

                // Tampilkan pesan
                if (message.isNotBlank()) {
                    Text(
                        text = message,
                        color = Color.Gray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(searchResults) { user ->
                        // Memeriksa apakah pengguna sudah diikuti
                        val isAlreadyFollowing = followedUserIds.contains(user.id)
                        UserSearchCard(
                            user = user,
                            isAlreadyFollowing = isAlreadyFollowing,
                            onAddFriendClick = { targetUserId, targetUsername ->
                                scope.launch { // Launch coroutine to call suspend function
                                    addFriend(targetUserId, targetUsername)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
