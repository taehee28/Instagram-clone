package com.thk.instagram_clone.ui.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.thk.data.repository.LoginRepository
import com.thk.data.util.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository
) : ViewModel() {
    val showLoading = MutableLiveData(false)
    val errorMessage = MutableLiveData("")
    val uiEvent: MutableLiveData<UiEvent?> = MutableLiveData(null)

    private val onStart = { showLoading.value = true }
    private val onSuccess = { uiEvent.value = UiEvent.LoginSuccess }
    private val onError = { msg: String? -> errorMessage.value = msg }
    private val onComplete = { showLoading.value = false }

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
        onError = onError,
        onComplete = onComplete
    )

    /**
     * Google 계정 정보로 Firebase 로그인
     */
    fun signIn(account: GoogleSignInAccount?) = loginRepository.signIn(
        account = account,
        onStart = onStart,
        onSuccess = onSuccess,
        onError = onError,
        onComplete = onComplete
    )

    sealed class UiEvent {
        object LoginSuccess: UiEvent()
    }
}

