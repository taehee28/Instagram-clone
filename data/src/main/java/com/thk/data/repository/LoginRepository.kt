package com.thk.data.repository

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.thk.data.util.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

interface LoginRepository {
    /**
     * 이메일, 비밀번호로 로그인
     */
    fun signIn(
        email: String,
        password: String,
        onStart: () -> Unit,
        onSuccess: () -> Unit,
        onError: (String?) -> Unit,
        onComplete: () -> Unit
    )

    /**
     * 구글 계정으로 로그인
     */
    fun signIn(
        account: GoogleSignInAccount?,
        onStart: () -> Unit,
        onSuccess: () -> Unit,
        onError: (String?) -> Unit,
        onComplete: () -> Unit
    )
}

class LoginRepositoryImpl : LoginRepository {
    override fun signIn(
        email: String,
        password: String,
        onStart: () -> Unit,
        onSuccess: () -> Unit,
        onError: (String?) -> Unit,
        onComplete: () -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            onStart()
            kotlin.runCatching {
                val result = Firebase.auth
                    .createUserWithEmailAndPassword(email, password)
                    .await()

                requireNotNull(result.user) { "Failed to signUp" }
            }.recoverCatching {
                when (it) {
                    is FirebaseAuthUserCollisionException -> signIn(email, password)
                    else -> {
                        it.printStackTrace()
                        onError(it.message)
                    }
                }
            }.onFailure {
                it.printStackTrace()
                onError(it.message)
            }.onSuccess {
                onSuccess()
            }.also {
                onComplete()
            }
        }
    }

    /**
     * 등록되어있는 이메일로 로그인
     */
    private suspend inline fun signIn(
        email: String,
        password: String
    ) {
        val result = Firebase.auth
            .signInWithEmailAndPassword(email, password)
            .await()

        requireNotNull(result.user) { "Failed to signIn" }
    }

    override fun signIn(
        account: GoogleSignInAccount?,
        onStart: () -> Unit,
        onSuccess: () -> Unit,
        onError: (String?) -> Unit,
        onComplete: () -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            onStart()
            kotlin.runCatching {
                val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
                val result = Firebase.auth
                    .signInWithCredential(credential)
                    .await()

                requireNotNull(result.user) { "Failed to signIn" }
            }.onFailure {
                it.printStackTrace()
                onError(it.message)
            }.onSuccess {
                onSuccess()
            }.also {
                onComplete()
            }
        }
    }
}