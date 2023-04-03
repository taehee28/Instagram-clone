package com.thk.instagram_clone.ui.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.thk.instagram_clone.ui.home.MainActivity
import com.thk.instagram_clone.R
import com.thk.instagram_clone.databinding.ActivityLoginBinding
import com.thk.instagram_clone.ui.login.LoginViewModel.UiEvent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private val TAG = LoginActivity::class.simpleName
    private lateinit var binding: ActivityLoginBinding

    private val loginViewModel: LoginViewModel by viewModels()
    @Inject lateinit var googleSignInClient: GoogleSignInClient

    /**
     * Google 로그인 후 결과 받는 callback
     */
    private val googleLoginLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        val result = it.data?.let { intent -> Auth.GoogleSignInApi.getSignInResultFromIntent(intent) } ?: return@registerForActivityResult
        if (result.isSuccess) {
            loginViewModel.signIn(result.signInAccount)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        setContentView(binding.root)

        binding.apply {
            lifecycleOwner = this@LoginActivity
            vm = loginViewModel
            activity = this@LoginActivity
        }

        loginViewModel.uiEvent.observe(this) {
            handleEvent(it)
        }
    }

    private fun handleEvent(event: UiEvent?) {
        event ?: return

        when (event) {
            is UiEvent.LoginSuccess -> moveToMainPage()
        }
    }

    /**
     * Google 계정에 로그인해서 계정 정보를 callback으로 받아옴
     */
    fun googleLogin() = googleLoginLauncher.launch(googleSignInClient.signInIntent)


    private fun moveToMainPage() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}