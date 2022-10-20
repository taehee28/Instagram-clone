package com.thk.instagram_clone.viewmodel

import android.widget.EditText
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thk.data.model.ContentDto
import com.thk.data.repository.MainRepository
import com.thk.data.util.logd
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class CommentViewModel @Inject constructor(
    private val mainRepository: MainRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val contentUid: String?
    private val destinationUid: String?

    val errorMessage = MutableLiveData("")

    init {
        contentUid = savedStateHandle.get<String>("contentUid")
        destinationUid = savedStateHandle.get<String>("destinationUid")
    }

    val comments = mainRepository.getComments(contentUid) { print(it) }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun sendComment(editText: EditText) {
        val text = editText.text.toString().ifBlank {
            errorMessage.value = "Comment can't be empty"
            return@sendComment
        }

        mainRepository
            .sendComment(text, contentUid)
            .onFailure { errorMessage.value = it.message }
            .onSuccess {
                it.invokeOnCompletion {
                    editText.setText("")
                    mainRepository.registerCommentAlarm(destinationUid, text)
                }
            }
    }
}