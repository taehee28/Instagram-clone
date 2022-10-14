@file:OptIn(ExperimentalCoroutinesApi::class)

package com.thk.instagram_clone.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.snapshots
import com.thk.data.model.ALARM_FOLLOW
import com.thk.data.model.AlarmDto
import com.thk.data.model.ContentDto
import com.thk.data.model.FollowDto
import com.thk.instagram_clone.util.FcmPush
import com.thk.data.util.PathString
import com.thk.data.util.Firebase
import com.thk.data.util.SystemString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.lang.String.format

class AccountViewModel(private val uid: String?) : ViewModel() {
    private val profileImageUrl = Firebase.firestore
        .collection(PathString.profileImages)
        .document(uid ?: "")
        .snapshots()
        .mapLatest { value ->
            value.data?.get("image").toString()
        }

    private val followData = Firebase.firestore
        .collection(PathString.users)
        .document(uid ?: "")
        .snapshots()
        .mapLatest { value ->
            value.toObject(FollowDto::class.java) ?: throw IllegalArgumentException("null returned")
        }

    private val postList = Firebase.firestore
        .collection(PathString.images)
        .whereEqualTo("uid", uid ?: "")
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

    /**
     * 프로필 사진 업로드.
     * AccountFragment에서만 사용
     */
    fun uploadProfileImage(imageUri: Uri?, callback: (() -> Unit)? = null) = imageUri?.also { Firebase.auth.currentUser?.uid?.also { uid ->
        Firebase.storage.reference.child(PathString.userProfileImages).child(uid).also { ref ->
            ref.putFile(imageUri)
                .continueWithTask { ref.downloadUrl }
                .addOnSuccessListener { uri ->
                    val map = mapOf("image" to uri.toString())
                    Firebase.firestore.collection(PathString.profileImages).document(uid).set(map)

                    callback?.invoke()
                }
        }
    } }

    /**
     * 팔로우/언팔로우 요청 처리
     */
    fun requestFollow(followDto: FollowDto) = kotlin.runCatching {
        val tsDocMyFollowing = Firebase.firestore.collection("users").document(Firebase.auth.currentUser?.uid!!)
        Firebase.firestore.runTransaction {
            val myFollow = it.get(tsDocMyFollowing).toObject(FollowDto::class.java) ?: FollowDto()
            if (myFollow.followings.containsKey(uid!!)) {
                it.set(
                    tsDocMyFollowing,
                    myFollow.copy(followingCount = myFollow.followingCount - 1).apply { followings.remove(uid!!) }
                )
            } else {
                it.set(
                    tsDocMyFollowing,
                    myFollow.copy(followingCount = myFollow.followingCount + 1).apply { followings[uid!!] = true }
                )
            }
        }

        val tsDocOthersFollower = Firebase.firestore.collection("users").document(uid!!)
        Firebase.firestore.runTransaction {
            if (followDto.followers.containsKey(Firebase.auth.currentUser?.uid)) {
                it.set(
                    tsDocOthersFollower,
                    followDto.copy(followerCount = followDto.followerCount - 1).apply { followers.remove(
                        Firebase.auth.currentUser?.uid) }
                )
            } else {
                it.set(
                    tsDocOthersFollower,
                    followDto.copy(followerCount = followDto.followerCount + 1).apply { followers[Firebase.auth.currentUser?.uid!!] = true }
                )

                registerFollowAlarm()
            }
        }
    }

    /**
     * 팔로우 시 상대방에게 푸시알람 전송 
     */
    private fun registerFollowAlarm() = uid?.let {
        val alarmDto = AlarmDto(
            destinationUid = it,
            userId = Firebase.auth.currentUser?.email ?: "",
            uid = Firebase.auth.currentUser?.uid ?: "",
            kind = ALARM_FOLLOW,
            timestamp = System.currentTimeMillis()
        )

        Firebase.firestore
            .collection("alarms")
            .document()
            .set(alarmDto)

        val msg = format(SystemString.ALARM_FOLLOW, Firebase.auth.currentUser?.uid)
        FcmPush.sendMessage(it, "Instagram-clone", msg)
    }
}

class AccountViewModelFactory(private val uid: String?) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        AccountViewModel(uid) as T
}