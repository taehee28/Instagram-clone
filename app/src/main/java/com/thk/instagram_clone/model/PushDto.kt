package com.thk.instagram_clone.model

data class PushDto(
    val to: String = "",
    val notification: Notification = Notification()
) {
    data class Notification(
        val title: String = "",
        val body: String = ""
    )
}
