package com.example.mycamera.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mycamera.data.ChatListItem // Impor ChatListItem
import com.example.mycamera.ui.components.BottomNavigationBar // Impor BottomNavigationBar
import com.example.mycamera.ui.components.ChatListItemCard // Impor ChatListItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListPage(navController: NavController) {
    val chatListItems = remember { mutableStateListOf<ChatListItem>() }

    // Dummy data untuk daftar chat
    LaunchedEffect(Unit) {
        chatListItems.addAll(
            listOf(
                ChatListItem(
                    userId = "rachel",
                    username = "Rachel",
                    profilePictureUrl = "https://placehold.co/40x40/FF6347/FFFFFF?text=R",
                    lastMessage = "Udah makan belum?",
                    lastMessageTimestamp = System.currentTimeMillis() - 3600000 // 1 jam lalu
                ),
                ChatListItem(
                    userId = "michael",
                    username = "Michael",
                    profilePictureUrl = "https://placehold.co/40x40/4682B4/FFFFFF?text=M",
                    lastMessage = "Dimana tuh tempatnya?",
                    lastMessageTimestamp = System.currentTimeMillis() - 7200000 // 2 jam lalu
                ),
                ChatListItem(
                    userId = "emma",
                    username = "Emma",
                    profilePictureUrl = "https://placehold.co/40x40/3CB371/FFFFFF?text=E",
                    lastMessage = "Aku beli di demangan sih",
                    lastMessageTimestamp = System.currentTimeMillis() - 10800000 // 3 jam lalu
                )
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat", fontWeight = FontWeight.Bold, color = Color(0xFF4C4C4C)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF5F0E7))
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
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
                items(chatListItems) { chatItem ->
                    ChatListItemCard(chatItem = chatItem) {
                        navController.navigate("chat_room/${chatItem.username}")
                    }
                }
            }
        }
    }
}
