// BottomNavigationBar.kt
package com.example.mycamera.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource // Impor MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
// Menghapus impor yang tidak lagi dibutuhkan karena tidak pakai R.drawable
// import androidx.compose.foundation.Image
// import androidx.compose.ui.res.painterResource
// import com.example.mycamera.R
import androidx.compose.material3.ripple // Impor ripple dari Material3
import androidx.compose.runtime.remember

// Data class untuk menampung properti item navigasi
data class NavItem(val route: String, val icon: ImageVector, val contentDescription: String)

@Composable
fun BottomNavigationBar(navController: NavController, modifier: Modifier = Modifier) {
    val currentRoute by navController.currentBackStackEntryAsState()
    val selectedRoute = currentRoute?.destination?.route

    // Daftar item navigasi
    val navItems = listOf(
        NavItem("search_users", Icons.Default.PersonAdd, "Add Friend"),
        NavItem("home", Icons.Default.Home, "Home"),
        NavItem("camera", Icons.Default.PhotoCamera, "Camera"),
        NavItem("liked", Icons.Default.Favorite, "Liked Photos"),
        NavItem("profile", Icons.Default.Person, "Profile")
    )

    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .clip(RoundedCornerShape(15.dp)),
        containerColor = Color(0xFFFDFAE7), // Warna latar belakang NavigationBar
        tonalElevation = 12.dp,
    ) {
        navItems.forEach { item ->
            val isSelected = selectedRoute == item.route
            Box(
                modifier = Modifier
                    .weight(1f) // Setiap item mengambil porsi lebar yang sama
//                    .fillMaxHeight() // Memenuhi tinggi NavigationBar
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() }, // Diperlukan untuk efek ripple
                        indication = ripple() // Menggunakan ripple standar Material3
                    ) { // Menambahkan fungsi klik untuk navigasi
                        navController.navigate(item.route) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                contentAlignment = Alignment.Center // Menengahkan ikon di dalam Box
            ) {
                if (isSelected) {
                    // Mengganti Image dengan Box yang memiliki background berbentuk kustom
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
//                            .height(48.dp) // Sesuaikan tinggi indikator agar sesuai
                            .clip(RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp, bottomStart = 15.dp, bottomEnd = 15.dp)) // Sudut membulat kustom untuk efek "rumah"
                            .background(Color(0xFFC8A877)) // Warna indikator
                            .align(Alignment.Center) // Pusatkan background ini di dalam Box item
                    )
                }
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.contentDescription,
                    tint = if (isSelected) Color.White else Color.Black, // Warna ikon berubah saat dipilih
                    modifier = Modifier.size(24.dp) // Ukuran ikon
                )
            }
        }
    }
}




//import androidx.compose.foundation.BorderStroke
//import androidx.compose.foundation.border
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material3.Icon
//import androidx.compose.material3.NavigationBar
//import androidx.compose.material3.NavigationBarItem
//import androidx.compose.material3.NavigationBarItemDefaults
//import androidx.compose.material3.Text
//import androidx.compose.material3.contentColorFor
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.unit.dp
//import androidx.navigation.NavController
//import androidx.navigation.compose.currentBackStackEntryAsState
//import kotlin.math.round
//
//@Composable
//fun BottomNavigationBar(navController: NavController) {
//    val currentRoute by navController.currentBackStackEntryAsState()
//    val selectedRoute = currentRoute?.destination?.route
//
//    val itemColors = NavigationBarItemDefaults.colors(
//        selectedIconColor = Color.White,
//        selectedTextColor = Color(0xFFB88C4A),
//        unselectedIconColor = Color.Black,
//        unselectedTextColor = Color.Gray,
//        indicatorColor = Color(0xFFC8A877)
//    )
//
//    NavigationBar(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 16.dp, vertical = 16.dp)
//            .clip(RoundedCornerShape(24.dp)),
//        containerColor = Color(0xFFFDFAE7), // Warna latar belakang BottomNav
//        tonalElevation = 12.dp,
//    ) {
//        NavigationBarItem(
//            modifier = Modifier
//                .weight(1f),
//            selected = selectedRoute == "search_users",
//            onClick = { navController.navigate("search_users") { launchSingleTop = true; restoreState = true } },
//            icon = { Icon(Icons.Default.PersonAdd, contentDescription = "Add Friend") },
////            label = { Text("Add") },
//            colors = itemColors
//        )
//        NavigationBarItem(
//            modifier = Modifier
//                .weight(1f),
//            selected = selectedRoute == "home",
//            onClick = { navController.navigate("home") { launchSingleTop = true; restoreState = true } },
//            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
////            label = { Text("Home") },
//            colors = itemColors
//        )
//        NavigationBarItem(
//            modifier = Modifier
//                .weight(1f),
//            selected = selectedRoute == "camera",
//            onClick = { navController.navigate("camera") { launchSingleTop = true; restoreState = true } },
//            icon = { Icon(Icons.Default.PhotoCamera, contentDescription = "Camera") },
////            label = { Text("Camera") },
//            colors = itemColors
//        )
//        NavigationBarItem(
//            modifier = Modifier
//                .weight(1f),
//            selected = selectedRoute == "liked",
//            onClick = { navController.navigate("liked") { launchSingleTop = true; restoreState = true } },
//            icon = { Icon(Icons.Default.Favorite, contentDescription = "Liked Photos") },
////            label = { Text("Liked") },
//            colors = itemColors
//        )
//        NavigationBarItem(
//            modifier = Modifier
//                .weight(1f),
//            selected = selectedRoute == "profile",
//            onClick = { navController.navigate("profile") { launchSingleTop = true; restoreState = true } },
//            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
////            label = { Text("Profile") },
//            colors = itemColors
//        )
//    }
//}
