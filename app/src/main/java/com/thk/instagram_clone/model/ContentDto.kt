package com.thk.instagram_clone.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class ContentDto(
    val description: String = "",
    val imageUrl: String = "",
    @Exclude val contentUid: String = "",
    val uid: String? = "",
    val userId: String? = "",
    val timestamp: Long = 0,
    val likeCount: Int = 0,
    val liked: Boolean = false
) {
    data class Comment(
        val uid: String = "",
        val userId: String = "",
        val text: String = "",
        val timestamp: Long = 0
    )
}
