@file:OptIn(ExperimentalCoroutinesApi::class)

package com.thk.instagram_clone.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.snapshots
import com.thk.data.model.ALARM_LIKE
import com.thk.data.model.AlarmDto
import com.thk.data.model.ContentDto
import com.thk.data.repository.MainRepository
import com.thk.instagram_clone.util.FcmPush
import com.thk.data.util.Firebase
import com.thk.data.util.SystemString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import okhttp3.internal.format
import javax.inject.Inject

@HiltViewModel
class DetailViewViewModel @Inject constructor(
    private val mainRepository: MainRepository
) : ViewModel() {

    /**
     * 사용자들이 업로드한 글 리스트를 가지는 Flow
     */
    val itemsFlow = mainRepository.getPosts { print(it) }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * 좋아요 버튼 눌렸을 때의 처리
     */
    fun onLikeClicked(contentUid: String?, isSelected: Boolean) {
        if (contentUid.isNullOrBlank()) return
        mainRepository.requestLike(contentUid, isSelected)
    }

    /**
     * 좋아요 버튼 눌렸을 때 상대방에게 알람이 가도록 처리
     */
    fun registerLikeAlarm(destinationUid: String?) {
        if (destinationUid.isNullOrBlank()) return

        // 알람 내용을 firestore에 저장
        val alarmDto = AlarmDto(
            destinationUid = destinationUid,
            userId = Firebase.auth.currentUser?.email ?: "",
            uid = Firebase.auth.currentUser?.uid ?: "",
            kind = ALARM_LIKE,
            timestamp = System.currentTimeMillis()
        )

        Firebase.firestore
            .collection("alarms")
            .document()
            .set(alarmDto)

        // 푸시 알람 전송
        val msg = format(SystemString.ALARM_FAVORITE, Firebase.auth.currentUser?.email ?: "")
        FcmPush.sendMessage(destinationUid, "Instagram-clone", msg)
    }

}