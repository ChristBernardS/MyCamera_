package com.example.mycamera.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow // Import TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mycamera.data.NotificationItem // Impor NotificationItem

@Composable
fun NotificationCard(notificationItem: NotificationItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f), // Make this Row take available space
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = notificationItem.profilePictureUrl,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color.Gray),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${notificationItem.username} send a friend request",
                fontSize = 15.sp,
                color = Color(0xFF4C4C4C),
                maxLines = 2, // Limit the text to a maximum of 2 lines
                overflow = TextOverflow.Ellipsis // Add ellipsis if the text overflows
            )
        }
        Spacer(modifier = Modifier.width(4.dp)) // Add a spacer between text and button
        Button(
            onClick = { /* TODO: Implement accept friend request */ },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB88C4A)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.width(110.dp) // Set a fixed width for the button
                .height(40.dp) // Set a fixed height for the button (adjust as needed)
        ) {
            Text("Accept", color = Color.White)
        }
    }
}