package com.example.mycamera

import android.content.res.Resources.Theme
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.mycamera.ui.screens.CameraPage
import com.example.mycamera.ui.screens.ChatListPage
import com.example.mycamera.ui.screens.ChatRoomPage
import com.example.mycamera.ui.screens.HomePage
import com.example.mycamera.ui.screens.LikedPhotosPage
import com.example.mycamera.ui.screens.LoginPage
import com.example.mycamera.ui.screens.NotificationPage
import com.example.mycamera.ui.screens.ProfilePage
import com.example.mycamera.ui.screens.RegisterPage
import com.example.mycamera.ui.screens.SearchUsersPage
import com.example.mycamera.ui.theme.MyCameraAppTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyCameraAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MyCameraApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCameraApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginPage(navController = navController) }
        composable("register") { RegisterPage(navController = navController) }
        composable("home") { HomePage(navController = navController) }
        composable("notifications") { NotificationPage(navController = navController) }
        composable("camera") { CameraPage(navController = navController) }
        composable("liked") { LikedPhotosPage(navController = navController) }
        composable("profile") { ProfilePage(navController = navController) }
        composable("search_users") { SearchUsersPage(navController = navController) }
        composable("chat_list") { ChatListPage(navController = navController) } // Rute baru untuk daftar chat
        composable("chat_room/{chatPartnerName}") { backStackEntry -> // Rute baru untuk ruang chat
            val chatPartnerName = backStackEntry.arguments?.getString("chatPartnerName") ?: "User"
            ChatRoomPage(navController = navController, chatPartnerName = chatPartnerName)
        }
    }
}