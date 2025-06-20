package com.example.mycamera.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.mycamera.data.ChatMessage
import com.example.mycamera.ui.components.MessageBubble

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomPage(navController: NavController, chatPartnerName: String) {
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var newMessageText by remember { mutableStateOf("") }

    // Dummy data pesan
    LaunchedEffect(Unit) {
        messages.addAll(
            listOf(
                ChatMessage(id = "m1", senderId = "other", senderName = chatPartnerName, text = "tu kopi yang lagi viral gk sih?", isSentByMe = false),
                ChatMessage(id = "m2", senderId = "me", senderName = "Me", text = "Iya, enak banget!", isSentByMe = true),
                ChatMessage(id = "m3", senderId = "other", senderName = chatPartnerName, text = "Kamu belinya dimana?", isSentByMe = false),
                ChatMessage(id = "m4", senderId = "me", senderName = "Me", text = "Aku beli di demangan sih", isSentByMe = true)
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = "https://placehold.co/40x40/3CB371/FFFFFF?text=E",
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Gray),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(chatPartnerName, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF4C4C4C))
                            Text("Today", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF5F0E7))
            )
        },
        containerColor = Color(0xFFF5F0E7)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                reverseLayout = false
            ) {
                items(messages) { message ->
                    MessageBubble(message = message)
                }
            }

            // Input pesan
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(Color(0xFF333333), RoundedCornerShape(24.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newMessageText,
                    onValueChange = { newMessageText = it },
                    placeholder = { Text("Tulis pesan...", color = Color.Gray) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.White
                    )
                )
                IconButton(
                    onClick = {
                        if (newMessageText.isNotBlank()) {
                            messages.add(
                                ChatMessage(
                                    senderId = "me",
                                    senderName = "Me",
                                    text = newMessageText,
                                    isSentByMe = true
                                )
                            )
                            newMessageText = ""
                        }
                    }
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send Message", tint = Color(0xFFB88C4A))
                }
            }
        }
    }
}