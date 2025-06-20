package com.example.mycamera.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.mycamera.data.ChatMessage // Impor ChatMessage

@Composable
fun MessageBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isSentByMe) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isSentByMe) Color(0xFFB88C4A) else Color(0xFF4C4C4C) // Warna gelembung
            ),
            modifier = Modifier.widthIn(max = 250.dp) // Lebar maksimum gelembung pesan
        ) {
            Text(
                text = message.text,
                color = if (message.isSentByMe) Color.White else Color.White, // Warna teks pesan
                modifier = Modifier.padding(10.dp)
            )
        }
    }
}