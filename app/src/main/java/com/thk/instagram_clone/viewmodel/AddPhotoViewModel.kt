package com.thk.instagram_clone.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thk.instagram_clone.model.ContentDto
import com.thk.instagram_clone.util.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoViewModel : ViewModel() {
    private var uri: Uri? = null

    fun setUri(uri: Uri?) {
        this.uri = uri
    }

    suspend fun uploadContent(text: String): Result<Unit> = kotlin.runCatching {
        requireNotNull(uri) { "No selected image" }

        val fileName = createFileName()
        val storageRef = Firebase.storage.reference.child("images").child(fileName)

            val uploadedUri = storageRef
                    .putFile(uri!!)
                    .await()
                    .storage
                    .downloadUrl
                    .await()
                    .toString()

            val contentDto = ContentDto(
                imageUrl = uploadedUri,
                uid = Firebase.auth.currentUser?.uid,
                userId = Firebase.auth.currentUser?.email,
                description = text,
                timestamp = System.currentTimeMillis()
            )

            // 포스트 자체를 저장
            Firebase.firestore.collection("images").document().set(contentDto).await()
    }

    private fun createFileName() = run {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        "Image_${timestamp}_.png"
    }
}