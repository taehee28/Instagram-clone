package com.thk.instagram_clone

import com.google.firebase.auth.FirebaseAuth

/**
 * Activity or Fragment마다 따로 instance 얻을 필요 없도록 하는 Object
 */
object FbAuth {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    operator fun invoke() = auth
}