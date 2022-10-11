package com.thk.instagram_clone.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.facebook.AccessToken
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.thk.instagram_clone.util.Firebase
import kotlinx.coroutines.flow.MutableStateFlow

class LoginViewModel : ViewModel() {
    private val TAG = LoginViewModel::class.simpleName

    val loginResultFlow = MutableStateFlow<LoginResult>(LoginResult.Init)

    /**
     * Firebase 로그인에 공통적으로 쓰이는 complete listener
     */
    private val onCompleteListener = { task: Task<AuthResult> ->
        when {
            task.isSuccessful -> {
                loginResultFlow.value = if (task.result.user != null) {
                    LoginResult.Success
                } else {
                    LoginResult.Error("Error: Login failed")
                }
            }
            else -> {
                Log.w(TAG, "onCompleteListener: Failure", task.exception)
                loginResultFlow.value = LoginResult.Error(task.exception?.message)
            }
        }
    }

    fun tryAutoLogin() {
        Firebase.auth.currentUser?.also {
            loginResultFlow.value = LoginResult.Success
        }
    }

    fun signUpWithEmail(email: String, password: String) {
        loginResultFlow.value = LoginResult.Loading

        Firebase.auth
            .createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                when {
                    task.isSuccessful -> {
                        loginResultFlow.value = if (task.result.user != null) {
                            LoginResult.Success
                        } else {
                            LoginResult.Error("Error: Login failed")
                        }
                    }
                    task.exception is FirebaseAuthUserCollisionException -> signInWithEmail(email, password)
                    task.exception is Exception -> {
                        Log.w(TAG, "signUpWithEmail: Failure", task.exception)
                        loginResultFlow.value = LoginResult.Error(task.exception?.message)
                    }
                }
            }
    }

    private fun signInWithEmail(email: String, password: String) {
        Firebase.auth
            .signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(onCompleteListener)
    }

    /**
     * Facebook 계정 정보로 Firebase 로그인
     */
    fun signInWithFacebookAccount(token: AccessToken?) = kotlin.runCatching {
        loginResultFlow.value = LoginResult.Loading

        val credential = FacebookAuthProvider.getCredential(token?.token!!)
        Firebase.auth
            .signInWithCredential(credential)
            .addOnCompleteListener(onCompleteListener)
    }.onFailure {
        it.printStackTrace()
        loginResultFlow.value = LoginResult.Error("Error: Login failed")
    }

    /**
     * Google 계정 정보로 Firebase 로그인
     */
    fun signInWithGoogleAccount(account: GoogleSignInAccount?) = kotlin.runCatching {
        loginResultFlow.value = LoginResult.Loading

        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        Firebase.auth
            .signInWithCredential(credential)
            .addOnCompleteListener(onCompleteListener)
    }.onFailure {
        it.printStackTrace()
        loginResultFlow.value = LoginResult.Error("Error: Login failed")
    }
}

sealed class LoginResult {
    object Init: LoginResult()
    object Loading: LoginResult()
    object Success: LoginResult()
    data class Error(val message: String?): LoginResult()
}