@file:OptIn(ExperimentalCoroutinesApi::class)

package com.thk.instagram_clone.viewmodel

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thk.data.model.ALARM_FOLLOW
import com.thk.data.model.AlarmDto
import com.thk.data.model.FollowDto
import com.thk.data.repository.MainRepository
import com.thk.data.util.Firebase
import com.thk.data.util.SystemString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.lang.String.format
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val mainRepository: MainRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val uid: String?

    val showLoading = MutableLiveData(false)
    val errorMessage = MutableLiveData("")

    init {
        uid = savedStateHandle.get<String>("uid")?.ifBlank { null } ?: Firebase.auth.currentUser?.uid
    }

    val profileImageUrl = mainRepository.getProfileImageUrl(uid) { print(it) }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    val followData = mainRepository.getFollowData(uid) { print(it) }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FollowDto()
        )

    val postList = mainRepository.getPosts(uid) { print(it) }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * 프로필 사진 업로드.
     * AccountFragment에서만 사용
     */
    fun uploadProfileImage(imageUri: Uri?) = viewModelScope.launch {
        mainRepository.uploadProfileImage(
            uri = imageUri,
            onStart = { showLoading.value = true },
            onComplete = { showLoading.value = false },
            onError = { errorMessage.value = it }
        )
    }
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
//        FcmPush.sendMessage(it, "Instagram-clone", msg)
    }
}

/*
class AccountViewModelFactory(private val uid: String?) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        AccountViewModel(uid) as T
}*/
