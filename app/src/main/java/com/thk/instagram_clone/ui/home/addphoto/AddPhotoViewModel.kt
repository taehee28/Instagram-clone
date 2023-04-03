package com.thk.instagram_clone.ui.home.addphoto

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thk.data.model.ContentDto
import com.thk.data.repository.MainRepository
import com.thk.data.util.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AddPhotoViewModel @Inject constructor(
    private val mainRepository: MainRepository
) : ViewModel() {
    val uri = MutableLiveData(Uri.parse(""))

    val showLoading = MutableLiveData(false)
    val errorMessage = MutableLiveData("")

    fun uploadContent(text: String?, onSuccess: () -> Unit) = viewModelScope.launch {
        mainRepository.uploadContent(
            photoUri = uri.value,
            text = text,
            onStart = { showLoading.value = true },
            onSuccess = onSuccess,
            onComplete = { showLoading.value = false },
            onError = { errorMessage.value = it }
        )
    }
}