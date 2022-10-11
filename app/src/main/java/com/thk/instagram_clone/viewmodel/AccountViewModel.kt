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
    private val profileImageUrl = Firebase.firestore
        .collection(PathString.profileImages)
        .document(Firebase.auth.currentUser?.uid ?: "")
        .snapshots()
        .mapLatest { value ->
            value.data?.get("image").toString()
        }

    private val followData = Firebase.firestore
        .collection(PathString.users)
        .document(Firebase.auth.currentUser?.uid ?: "")
        .snapshots()
        .mapLatest { value ->
            value.toObject(FollowDto::class.java) ?: throw IllegalArgumentException("null returned")
        }

    private val postList = Firebase.firestore
        .collection(PathString.images)
        .whereEqualTo("uid", Firebase.auth.currentUser?.uid ?: "")
        .snapshots()
        .mapLatest { value ->
            value.documents.map {
                it.toObject(ContentDto::class.java)?.copy(contentUid = it.id)
                    ?: throw IllegalArgumentException("null returned")
            }
        }

    /**
     * 프로필 이미지, 팔로우 정보, 올린 글 리스트 Flow들을 하나로 합친 StateFlow
     */
    val accountDataFlow = merge(
        profileImageUrl,
        followData,
        postList
    ).catch {
        it.printStackTrace()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Unit
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