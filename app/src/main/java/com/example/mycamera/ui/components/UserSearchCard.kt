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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mycamera.data.User // Import User data class

@Composable
fun UserSearchCard(
    user: User,
    isAlreadyFollowing: Boolean, // Parameter ini yang diperlukan
    onAddFriendClick: (userId: String, username: String) -> Unit // Parameter ini juga diperlukan
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = user.profilePictureUrl,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Gray),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(user.username, fontSize = 16.sp, color = Color(0xFF4C4C4C))
        }
        Button(
            onClick = { onAddFriendClick(user.id, user.username) },
            enabled = !isAlreadyFollowing, // Nonaktifkan tombol jika sudah mengikuti
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB88C4A)),
            shape = RoundedCornerShape(24.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Text(
                text = if (isAlreadyFollowing) "Following" else "Add", // Ubah teks tombol
                color = Color.White
            )
        }
    }
}