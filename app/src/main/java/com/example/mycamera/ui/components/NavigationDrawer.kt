package com.example.mycamera.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.mycamera.R
import com.example.mycamera.data.User // Impor data class User
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationDrawer(
    drawerState: DrawerState,
    scope: CoroutineScope,
    navController: NavController,
    auth: FirebaseAuth,
    googleSignInClient: GoogleSignInClient,
    // --- PERUBAHAN 1: Tambahkan parameter untuk menerima data profil ---
    userProfile: User?, // Dibuat nullable (?) karena data mungkin sedang loading
    content: @Composable () -> Unit
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFFFDFAE7),
                modifier = Modifier.width(280.dp),
                drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
            ) {
                // --- PERUBAHAN 2: Tampilkan data asli atau loading state ---
                if (userProfile != null) {
                    // Jika data user sudah ada, tampilkan
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Menu",
                            modifier = Modifier
                                .align(Alignment.End)
                                .clickable { scope.launch { drawerState.close() } }
                                .padding(bottom = 8.dp),
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        AsyncImage(
                            // Gunakan data dari userProfile, beri fallback jika kosong
                            model = userProfile.profilePictureUrl.ifEmpty { "https://placehold.co/60x60/87CEEB/FFFFFF?text=${userProfile.username.firstOrNull()?.toUpperCase() ?: '?'}" },
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(Color.Gray),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(userProfile.username, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF4C4C4C))
                    }
                } else {
                    // Jika data user masih loading, tampilkan placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(150.dp), // Beri tinggi agar konsisten
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                Divider(color = Color(0xFFB88C4A), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

                // Item menu lainnya (tidak berubah)
                NavigationDrawerItem(
                    label = { Text("Switch dark mode", color = Color(0xFF4C4C4C)) },
                    icon = { Icon(Icons.Default.DarkMode, contentDescription = "Dark Mode", tint = Color.Black) },
                    selected = false,
                    onClick = { /* TODO: Implement dark mode switch */ },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Notification", color = Color(0xFF4C4C4C)) },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Notification", tint = Color.Black) },
                    selected = false,
                    onClick = { navController.navigate("notifications") },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Settings", color = Color(0xFF4C4C4C)) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.Black) },
                    selected = false,
                    onClick = { /* TODO: Implement settings */ },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Chat", color = Color(0xFF4C4C4C)) },
                    icon = { Icon(Icons.Default.ChatBubble, contentDescription = "Chat", tint = Color.Black) },
                    selected = false,
                    onClick = { navController.navigate("chat_list") },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                NavigationDrawerItem(
                    label = { Text("Logout", color = Color.Black) },
                    icon = { Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.Black) },
                    selected = false,
                    onClick = {
                        auth.signOut()
                        googleSignInClient.signOut()
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                )
            }
        },
        content = content
    )
}