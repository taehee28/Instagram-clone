@file:OptIn(ExperimentalCoroutinesApi::class)

package com.thk.instagram_clone.viewmodel

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thk.data.model.FollowDto
import com.thk.data.repository.MainRepository
import com.thk.data.util.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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
        mainRepository.requestFollow(uid!!, followDto)
    }
}

/*
class AccountViewModelFactory(private val uid: String?) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        AccountViewModel(uid) as T
}*/
