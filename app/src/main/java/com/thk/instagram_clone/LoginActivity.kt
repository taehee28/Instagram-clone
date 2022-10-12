package com.thk.instagram_clone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.thk.instagram_clone.databinding.ActivityLoginBinding
import com.thk.instagram_clone.util.LoadingDialog
import com.thk.instagram_clone.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private val TAG = LoginActivity::class.simpleName
    private lateinit var binding: ActivityLoginBinding

    private val loginViewModel: LoginViewModel by viewModels()
    @Inject lateinit var googleSignInClient: GoogleSignInClient
    @Inject lateinit var fbCallbackManager: CallbackManager

    /**
     * Google 로그인 후 결과 받는 callback
     */
    private val googleLoginLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        val result = it.data?.let { intent -> Auth.GoogleSignInApi.getSignInResultFromIntent(intent) } ?: return@registerForActivityResult
        if (result.isSuccess) {
            loginViewModel.signInWithGoogleAccount(result.signInAccount)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            btnLoginEmail.setOnClickListener {
                loginViewModel.signUpWithEmail(
                    binding.etEmail.text.toString(),
                    binding.etPassword.text.toString()
                )
            }
            btnLoginGoogle.setOnClickListener { googleLogin() }
            btnLoginFacebook.setOnClickListener { facebookLogin() }
        }

        lifecycleScope.launch {
            loginViewModel.loginResultFlow
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collectLatest {
                    if (LoadingDialog.isShowing) LoadingDialog.dismiss()

                    when (it) {
                        com.thk.instagram_clone.viewmodel.LoginResult.Init -> {}
                        com.thk.instagram_clone.viewmodel.LoginResult.Loading -> {
                            /* todo: show indicator */
                            LoadingDialog.show(this@LoginActivity)
                        }
                        com.thk.instagram_clone.viewmodel.LoginResult.Success -> {
                            moveToMainPage()
                        }
                        is com.thk.instagram_clone.viewmodel.LoginResult.Error -> {
                            Toast.makeText(this@LoginActivity, it.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
        }
    }

    override fun onStart() {
        super.onStart()
        // 자동로그인 시도
        loginViewModel.tryAutoLogin()
    }

    /**
     * Facebook 계정에 로그인해서 계정 정보 받아옴
     */
    private fun facebookLogin() {
        // activity를 인자로 넘겨주어야 해서 viewModel로 이전 못함
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("public_profile", "email"))
        LoginManager.getInstance().registerCallback(fbCallbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult?) {
                loginViewModel.signInWithFacebookAccount(result?.accessToken)
            }

            override fun onCancel() {
                Log.i(TAG, "onCancel: Facebook login is canceled")
            }

            override fun onError(error: FacebookException?) {
                error?.printStackTrace()
            }

        })
    }

    /**
     * Google 계정에 로그인해서 계정 정보를 callback으로 받아옴
     */
    private fun googleLogin() = googleLoginLauncher.launch(googleSignInClient.signInIntent)


    private fun moveToMainPage() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}