@file:OptIn(ExperimentalCoroutinesApi::class)

package com.thk.instagram_clone.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thk.data.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class DetailViewViewModel @Inject constructor(
    private val mainRepository: MainRepository
) : ViewModel() {

    /**
     * 사용자들이 업로드한 글 리스트를 가지는 Flow
     */
    val itemsFlow = mainRepository.getPosts { print(it) }
        .distinctUntilChanged()
        .stateIn(
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
}