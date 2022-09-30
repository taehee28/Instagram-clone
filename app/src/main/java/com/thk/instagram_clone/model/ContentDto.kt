package com.thk.instagram_clone.model

data class ContentDto(
    val description: String = "",
    val imageUrl: String = "",
    val uid: String? = "",
    val userId: String? = "",
    val timestamp: Long = 0,
    val likeCount: Int = 0,
    val isLiked: Boolean = false
) {
    data class Comment(
        val uid: String = "",
        val userId: String = "",
        val text: String = "",
        val timestamp: Long = 0
    )
}
