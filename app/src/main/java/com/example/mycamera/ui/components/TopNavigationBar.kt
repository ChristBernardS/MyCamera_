package com.example.mycamera.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    titleString: String,
    drawerState: DrawerState,
    scope: CoroutineScope,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(
            titleString,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4C4C4C),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        ) },
        navigationIcon = {
            IconButton(onClick = {
//                Log.d("HomePage", "Burger icon clicked to open drawer")
                scope.launch { drawerState.open() }
            }) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = Color.Black
                )
            }
        },
        actions = {
            // Ikon Chat di kanan atas
            IconButton(onClick = {
//                Log.d("HomePage", "Chat icon clicked to navigate to chat_list")
                navController.navigate("chat_list")
            }) {
                Icon(
                    Icons.Default.ChatBubble,
                    contentDescription = "Chat",
                    tint = Color.Black
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF5F0E7)) // Warna top app bar
    )
}