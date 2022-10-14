package com.thk.data.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

data class ContentDto(
    val description: String = "",
    val imageUrl: String = "",
    @get:Exclude val contentUid: String = "",
    val uid: String? = "",
    val userId: String? = "",
    val timestamp: Long = 0,
    val likeCount: Int = 0,
    val likedUsers: MutableMap<String, Boolean> = mutableMapOf()
) {
    data class Comment(
        val uid: String = "",
        val userId: String = "",
        val text: String = "",
        val timestamp: Long = 0
    )
}
