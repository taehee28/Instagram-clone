@file:OptIn(ExperimentalCoroutinesApi::class)

package com.thk.instagram_clone.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.snapshots
import com.thk.instagram_clone.model.ContentDto
import com.thk.instagram_clone.model.FollowDto
import com.thk.instagram_clone.util.PathString
import com.thk.instagram_clone.util.Firebase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

class AccountViewModel : ViewModel() {
    val profileImageUrl = Firebase.firestore
        .collection(PathString.profileImages)
        .document(Firebase.auth.currentUser?.uid ?: "")
        .snapshots()
        .mapLatest { value ->
            val url = value.data?.get("image") ?: throw IllegalArgumentException("null returned")
            AccountData.ProfileImageData(url)
        }.catch {
            it.printStackTrace()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AccountData.ProfileImageData(Any())
        )

    val followData = Firebase.firestore
        .collection(PathString.users)
        .document(Firebase.auth.currentUser?.uid ?: "")
        .snapshots()
        .mapLatest { value ->
            val data = value.toObject(FollowDto::class.java) ?: throw IllegalArgumentException("null returned")
            AccountData.FollowData(data)
        }.catch {
            it.printStackTrace()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AccountData.FollowData(FollowDto())
        )

    val postList = Firebase.firestore
        .collection(PathString.images)
        .whereEqualTo("uid", Firebase.auth.currentUser?.uid ?: "")
        .snapshots()
        .mapLatest { value ->
            val list = value.documents.map {
                it.toObject(ContentDto::class.java)?.copy(contentUid = it.id)
                    ?: throw IllegalArgumentException("null returned")
            }

            AccountData.PostsData(list)
        }.catch {
            it.printStackTrace()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AccountData.PostsData(emptyList())
        )

    fun uploadProfileImage(imageUri: Uri?) = imageUri?.also { Firebase.auth.currentUser?.uid?.also { uid ->
        Firebase.storage.reference.child(PathString.userProfileImages).child(uid).also { ref ->
            ref.putFile(imageUri)
                .continueWithTask { ref.downloadUrl }
                .addOnSuccessListener { uri ->
                    val map = mapOf("image" to uri.toString())
                    Firebase.firestore.collection(PathString.profileImages).document(uid).set(map)
                }
        }
    } }

}

sealed class AccountData {
    data class ProfileImageData(val url: Any): AccountData()
    data class FollowData(val data: FollowDto): AccountData()
    data class PostsData(val list: List<ContentDto>): AccountData()
}
