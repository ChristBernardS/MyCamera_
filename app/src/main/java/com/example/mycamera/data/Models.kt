package com.example.mycamera.data

data class FeedItem(
    val id: String,
    val username: String,
    val profilePictureUrl: String,
    val imageUrl: Any, // Diubah dari String menjadi Any untuk mengakomodasi Int resource ID atau String URL
    val location: String,
    val date: String,
    val description: String,
    val likesCount: Int,
    val comments: List<Comment>,
    var isLiked: Boolean = false
)

// Data kelas untuk Komentar
data class Comment(
    val username: String,
    val text: String
)

// Data kelas untuk Notifikasi
data class NotificationItem(
    val id: String,
    val username: String,
    val profilePictureUrl: String,
    val type: String // "friend_request"
)

// Data kelas untuk Pengguna
data class User(
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val profilePictureUrl: String = "",
    // --- PERUBAHAN DI SINI ---
    // Diubah dari Map<String, Boolean> menjadi List<String> agar cocok
    // dengan struktur Array di database Firestore Anda.
    val followers: List<String> = emptyList(),
    val following: List<String> = emptyList()
) {
    // Constructor tanpa argumen diperlukan oleh Firestore.
    // Disesuaikan dengan perubahan tipe data di atas.
    constructor() : this("", "", "", "", emptyList(), emptyList())
}

// Data kelas untuk Pesan Chat
data class ChatMessage(
    val id: String = "",
    val senderId: String,
    val senderName: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSentByMe: Boolean = false // Ini akan ditentukan di UI
)

// Data kelas untuk Item Daftar Chat
data class ChatListItem(
    val userId: String,
    val username: String,
    val profilePictureUrl: String,
    val lastMessage: String,
    val lastMessageTimestamp: Long
)