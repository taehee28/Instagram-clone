package com.thk.instagram_clone.model

data class ContentDto(
    val description: String,
    val imageUrl: String,
    val uid: String,
    val userId: String,
    val timestamp: Long,
    val favoriteCount: Int = 0
) {
    data class Comment(
        val uid: String,
        val userId: String,
        val text: String,
        val timestamp: Long
    )
}
