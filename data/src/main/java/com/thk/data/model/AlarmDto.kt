package com.thk.data.model

data class AlarmDto(
    val destinationUid: String = "",
    val userId: String = "",
    val uid: String = "",
    val kind: Int = -1,
    val message: String = "",
    val timestamp: Long = 0
)

object AlarmKind {
    const val ALARM_LIKE = 0
    const val ALARM_COMMENT = 1
    const val ALARM_FOLLOW = 2
}