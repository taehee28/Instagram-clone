@file:OptIn(ExperimentalCoroutinesApi::class)

package com.thk.data.repository

import android.net.Uri
import com.google.firebase.firestore.ktx.snapshots
import com.thk.data.model.*
import com.thk.data.remote.FcmPush
import com.thk.data.util.Firebase
import com.thk.data.util.PathString
import com.thk.data.util.SystemString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.tasks.await
import java.lang.String.format
import java.text.SimpleDateFormat
import java.util.*

interface MainRepository {
    /**
     * 모든 포스트 얻기
     */
    fun getPosts(onError: (String?) -> Unit): Flow<List<ContentDto>>

    /**
     * 특정 uid의 포스트 얻기
     */
    fun getPosts(uid: String?, onError: (String?) -> Unit): Flow<List<ContentDto>>

    /**
     * 특정 uid의 팔로우 정보 얻기
     */
    fun getFollowData(uid: String?, onError: (String?) -> Unit): Flow<FollowDto>

    /**
     * 특정 uid의 프로필 이미지 url 얻기
     */
    fun getProfileImageUrl(uid: String?, onError: (String?) -> Unit): Flow<String>

    /**
     * 프로필 사진 업로드
     */
    suspend fun uploadProfileImage(
        uri: Uri?,
        onStart: () -> Unit,
        onComplete: () -> Unit,
        onError: (String?) -> Unit
    )

    /**
     * 팔로우/언팔로우 요청 처리 
     */
    fun requestFollow(targetUid: String, followDto: FollowDto)

    /**
     * 좋아요 처리
     */
    fun requestLike(contentUid: String, isSelected: Boolean)

    fun getAlarmList(onError: (String?) -> Unit): Flow<List<AlarmDto>>

    suspend fun uploadContent(
        photoUri: Uri?,
        text: String?,
        onStart: () -> Unit,
        onSuccess: () -> Unit,
        onComplete: () -> Unit,
        onError: (String?) -> Unit
    )
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

    override fun getPosts(uid: String?, onError: (String?) -> Unit): Flow<List<ContentDto>> = kotlin.runCatching {
        requireNotNull(uid)

        Firebase.firestore
            .collection(PathString.images)
            .whereEqualTo("uid", uid)
            .snapshots()
            .mapLatest { value ->
                value.documents.map {
                    it.toObject(ContentDto::class.java)?.copy(contentUid = it.id)
                        ?: throw IllegalArgumentException("null returned")
                }
            }.catch {
                it.printStackTrace()
            }

    }.onFailure {
        onError(it.message)
    }.getOrDefault(emptyFlow())

    override fun getFollowData(uid: String?, onError: (String?) -> Unit): Flow<FollowDto> = kotlin.runCatching {
        requireNotNull(uid)

        Firebase.firestore
            .collection(PathString.users)
            .document(uid)
            .snapshots()
            .mapLatest { value ->
                value.toObject(FollowDto::class.java) ?: throw IllegalArgumentException("null returned")
            }.catch {
                it.printStackTrace()
            }

    }.onFailure {
        it.printStackTrace()
    }.getOrDefault(emptyFlow())

    override fun getProfileImageUrl(uid: String?, onError: (String?) -> Unit): Flow<String>  = kotlin.runCatching {
        requireNotNull(uid)

        Firebase.firestore
            .collection(PathString.profileImages)
            .document(uid)
            .snapshots()
            .mapLatest { value ->
                value.data?.get("image").toString()
            }.catch {
                it.printStackTrace()
            }

    }.onFailure {
        it.printStackTrace()
    }.getOrDefault(emptyFlow())

    override suspend fun uploadProfileImage(
        uri: Uri?,
        onStart: () -> Unit,
        onComplete: () -> Unit,
        onError: (String?) -> Unit
    ) {
        kotlin.runCatching {
            val uid = requireNotNull(Firebase.auth.currentUser?.uid)
            requireNotNull(uri)

            Firebase.storage
                .reference
                .child(PathString.userProfileImages)
                .child(uid)
                .also { ref ->
                    val downloadUri = ref.putFile(uri)
                        .await()
                        .storage
                        .downloadUrl
                        .await()
                        .toString()

                    val map = mapOf("image" to downloadUri)
                    Firebase.firestore.collection(PathString.profileImages).document(uid).set(map)
                }
        }.onFailure {
            onError(it.message)
        }.also {
            onComplete()
        }
    }

    override fun requestFollow(targetUid: String, followDto: FollowDto) {
        val tsDocMyFollowing = Firebase.firestore.collection("users").document(Firebase.auth.currentUser?.uid!!)
        Firebase.firestore.runTransaction {
            val myFollow = it.get(tsDocMyFollowing).toObject(FollowDto::class.java) ?: FollowDto()

            val modified = if (myFollow.followings.containsKey(targetUid)) {
                myFollow.copy(followingCount = myFollow.followingCount - 1).apply { followings.remove(targetUid) }
            } else {
                myFollow.copy(followingCount = myFollow.followingCount + 1).apply { followings[targetUid] = true }
            }

            it.set(
                tsDocMyFollowing,
                modified
            )
        }

        val tsDocOthersFollower = Firebase.firestore.collection("users").document(targetUid)
        Firebase.firestore.runTransaction {
            val isContains = followDto.followers.containsKey(Firebase.auth.currentUser?.uid)
            val modified = if (isContains) {
                followDto
                    .copy(followerCount = followDto.followerCount - 1)
                    .apply { followers.remove(Firebase.auth.currentUser?.uid) }
            } else {
                followDto
                    .copy(followerCount = followDto.followerCount + 1)
                    .apply { followers[Firebase.auth.currentUser?.uid!!] = true }
            }

            it.set(
                tsDocOthersFollower,
                modified
            )

            if (isContains) registerFollowAlarm(targetUid)
        }
    }

    /**
     * 팔로우 시 상대방에게 푸시알람 전송
     */
    private fun registerFollowAlarm(targetUid: String) {
        val alarmDto = AlarmDto(
            destinationUid = targetUid,
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
        FcmPush.sendMessage(targetUid, "Instagram-clone", msg)
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

    override fun getAlarmList(onError: (String?) -> Unit): Flow<List<AlarmDto>> = kotlin.runCatching {
        val uid = requireNotNull(Firebase.auth.currentUser?.uid)

        Firebase.firestore
            .collection("alarms")
            .whereEqualTo("destinationUid", uid)
            .snapshots()
            .mapLatest { value ->
                value.documents.map {
                    it.toObject(AlarmDto::class.java)
                        ?: throw IllegalArgumentException("null returned")
                }
            }.catch {
                it.printStackTrace()
                onError(it.message)
            }

    }.onFailure {
        it.printStackTrace()
        onError(it.message)
    }.getOrDefault(emptyFlow())

    override suspend fun uploadContent(
        photoUri: Uri?,
        text: String?,
        onStart: () -> Unit,
        onSuccess: () -> Unit,
        onComplete: () -> Unit,
        onError: (String?) -> Unit
    ) {
        kotlin.runCatching {
            onStart()

            requireNotNull(photoUri) { "Photo must be selected" }
            require(!text.isNullOrBlank()) { "Description must not be empty" }

            val fileName = createFileName()
            val storageRef = Firebase.storage.reference.child("images").child(fileName)

            val uploadedUri = storageRef
                .putFile(photoUri)
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
        }.onSuccess {
            onSuccess()
        }.onFailure {
            onError(it.message)
        }.also {
            onComplete()
        }
    }

    private fun createFileName() = run {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        "Image_${timestamp}_.png"
    }
}