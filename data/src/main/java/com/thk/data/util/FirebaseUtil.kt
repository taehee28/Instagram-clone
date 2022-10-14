package com.thk.data.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

/**
 * Activity or Fragment마다 따로 instance 얻을 필요 없도록 하는 Object
 */
object Firebase {
    val auth get() = FirebaseAuth.getInstance()
    val firestore get() = FirebaseFirestore.getInstance()
    val storage get() = FirebaseStorage.getInstance()
}