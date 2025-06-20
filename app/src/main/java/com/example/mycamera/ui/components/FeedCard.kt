package com.example.mycamera.ui.components

import androidx.compose.animation.Crossfade // Impor Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable // Impor clickable
import androidx.compose.foundation.interaction.MutableInteractionSource // Impor MutableInteractionSource (masih diperlukan untuk remember, tapi bisa dihapus jika tidak digunakan secara eksplisit di clickable)
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.* // Impor semua runtime (termasuk remember, mutableStateOf)
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mycamera.data.FeedItem // Impor FeedItem
import androidx.compose.material.ripple.rememberRipple // Impor rememberRipple (akan dihapus penggunaannya)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedCard(
    feedItem: FeedItem,
    modifier: Modifier = Modifier,
    onLikeClick: (String, Boolean) -> Unit // Callback: post ID, new isLiked status
) {
    // Gunakan state lokal untuk likesCount dan isLiked agar UI segera diperbarui
    var currentLikesCount by remember { mutableStateOf(feedItem.likesCount) }
    var currentIsLiked by remember { mutableStateOf(feedItem.isLiked) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Header Feed (Profil pengguna, dll.)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = feedItem.profilePictureUrl,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Gray),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(feedItem.username, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF4C4C4C))
                    Text(feedItem.location, fontSize = 12.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    modifier = Modifier.size(20.dp)
                )
            }

            // Gambar/Video Feed
            Crossfade(targetState = feedItem.imageUrl, label = "ImageFade") { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Post Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentScale = ContentScale.Crop
                )
            }

            // Interaksi (Like, Komentar)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tombol Like
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Like",
                    tint = if (currentIsLiked) Color.Red else Color.Gray, // Warna hati merah jika disukai
                    modifier = Modifier
                        .size(24.dp)
                        .clickable( // Membuat ikon bisa diklik
                            onClick = {
                                currentIsLiked = !currentIsLiked // Toggle status like lokal
                                if (currentIsLiked) {
                                    currentLikesCount++
                                } else {
                                    currentLikesCount--
                                }
                                onLikeClick(feedItem.id, currentIsLiked) // Panggil callback ke parent
                            }
                            // Hapus parameter interactionSource dan indication untuk menggunakan ripple default
                        )
                        .padding(4.dp) // Menambahkan padding untuk memperbesar area sentuh
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("$currentLikesCount", color = Color.Gray) // Tampilkan jumlah like dari state lokal
                Spacer(modifier = Modifier.width(16.dp))
                // Tombol Komentar
                Icon(
                    imageVector = Icons.Default.Comment,
                    contentDescription = "Comment",
                    tint = Color.Gray,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable( // Membuat ikon bisa diklik
                            onClick = { /* TODO: Implement comment action */ }
                            // Hapus parameter interactionSource dan indication untuk menggunakan ripple default
                        )
                        .padding(4.dp) // Menambahkan padding untuk memperbesar area sentuh
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("${feedItem.comments.size}", color = Color.Gray)
            }

            // Deskripsi Feed
            Text(
                text = feedItem.description,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                color = Color(0xFF4C4C4C)
            )

            // Komentar
            feedItem.comments.forEach { comment ->
                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)) {
                    Text(text = comment.username, fontWeight = FontWeight.Bold, color = Color(0xFF4C4C4C))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = comment.text, color = Color(0xFF4C4C4C))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}