@file:OptIn(ExperimentalCoroutinesApi::class)

package com.thk.data.repository

import android.util.Log
import com.google.firebase.firestore.ktx.snapshots
import com.thk.data.model.ALARM_LIKE
import com.thk.data.model.AlarmDto
import com.thk.data.model.ContentDto
import com.thk.data.remote.FcmPush
import com.thk.data.util.Firebase
import com.thk.data.util.SystemString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.mapLatest
import java.lang.String.format

interface MainRepository {
    fun getPosts(onError: (String?) -> Unit): Flow<List<ContentDto>>
    fun requestLike(contentUid: String, isSelected: Boolean)

}

class MainRepositoryImpl : MainRepository {
    override fun getPosts(onError: (String?) -> Unit) = Firebase.firestore
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
            onError(it.message)
        }

    override fun requestLike(contentUid: String, isSelected: Boolean) {
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

            registerLikeAlarm(item.uid)
        }
    }

    private fun registerLikeAlarm(destinationUid: String?) {
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