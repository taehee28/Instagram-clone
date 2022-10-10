package com.thk.instagram_clone.model

data class AlarmDto(
    val destinationUid: String = "",
    val userId: String = "",
    val uid: String = "",
    val kind: Int = 0,
    val message: String = "",
    val timestamp: Long = 0
)
