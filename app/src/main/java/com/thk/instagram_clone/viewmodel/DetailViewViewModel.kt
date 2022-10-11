@file:OptIn(ExperimentalCoroutinesApi::class)

package com.thk.instagram_clone.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.snapshots
import com.thk.instagram_clone.R
import com.thk.instagram_clone.model.ALARM_LIKE
import com.thk.instagram_clone.model.AlarmDto
import com.thk.instagram_clone.model.ContentDto
import com.thk.instagram_clone.util.FcmPush
import com.thk.instagram_clone.util.Firebase
import com.thk.instagram_clone.util.SystemString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import okhttp3.internal.format

class DetailViewViewModel : ViewModel() {

    /**
     * 사용자들이 업로드한 글 리스트를 가지는 Flow
     */
    val itemsFlow = Firebase.firestore
        .collection("images")
        .orderBy("timestamp")
        .snapshots()
        .mapLatest { value ->
            value.documents.map {
                it.toObject(ContentDto::class.java)?.copy(contentUid = it.id)
                    ?: throw IllegalArgumentException("null returned")
            }
        }.catch {
            it.printStackTrace()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * 좋아요 버튼 눌렸을 때의 처리
     */
    fun onLikeClicked(contentUid: String?, isSelected: Boolean) {
        if (contentUid.isNullOrBlank()) return

        val tsDoc = Firebase.firestore.collection("images").document(contentUid)

        Firebase.firestore.runTransaction { transaction ->
            val item = transaction.get(tsDoc).toObject(ContentDto::class.java) ?: return@runTransaction

            val isContains = Firebase.auth.currentUser?.uid?.let {
                if (isSelected) item.likedUsers.put(it, true) else item.likedUsers.remove(it)
                item.likedUsers.contains(it)
            }

            // null이라는 것은 로그인된 유저의 uid가 없다는것 == 비정상
            isContains ?: return@runTransaction

            transaction.set(
                tsDoc,
                item.copy(
                    likeCount = item.likeCount + if (isContains) 1 else -1
                )
            )
        }
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