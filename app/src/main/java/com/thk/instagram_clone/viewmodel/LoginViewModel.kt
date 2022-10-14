package com.thk.instagram_clone.viewmodel

import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.thk.data.repository.LoginRepository
import com.thk.data.util.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository
) : ViewModel() {
    val isLoading = ObservableBoolean(false)
    val errorMessage = ObservableField("")
    val uiEvent: MutableLiveData<UiEvent?> = MutableLiveData(null)

    private val onStart = { isLoading.set(true) }
    private val onSuccess = {
        isLoading.set(false)
        uiEvent.value = UiEvent.LoginSuccess
    }
    private val onError = { msg: String? ->
        isLoading.set(false)
        errorMessage.set(msg)
    }

    init {
        // 자동로그인
        Firebase.auth.currentUser?.also {
            uiEvent.value = UiEvent.LoginSuccess
        }
    }

    fun signIn(email: String, password: String) = loginRepository.signIn(
        email = email,
        password = password,
        onStart = onStart,
        onSuccess = onSuccess,
        onError = onError
    )

    /**
     * Google 계정 정보로 Firebase 로그인
     */
    fun signIn(account: GoogleSignInAccount?) = loginRepository.signIn(
        account = account,
        onStart = onStart,
        onSuccess = onSuccess,
        onError = onError
    )

    sealed class UiEvent {
        object LoginSuccess: UiEvent()
    }
}

