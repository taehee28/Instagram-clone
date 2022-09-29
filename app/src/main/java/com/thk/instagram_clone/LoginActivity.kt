package com.thk.instagram_clone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.thk.instagram_clone.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private val TAG = LoginActivity::class.simpleName
    private lateinit var binding: ActivityLoginBinding

    /**
     * Google 로그인 client
     */
    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        GoogleSignIn.getClient(this, gso)
    }

    /**
     * Facebook 로그인 callback manager
     */
    private val fbCallbackManager: CallbackManager by lazy { CallbackManager.Factory.create() }

    /**
     * Firebase 로그인에 공통적으로 쓰이는 complete listener
     */
    private val onCompleteListener = { task: Task<AuthResult> ->
        when {
            task.isSuccessful -> moveToMainPage(task.result.user)
            else -> {
                Log.w(TAG, "signInAndSignUp: Failure", task.exception)
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Google 로그인 후 결과 받는 callback
     */
    private val googleLoginLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        val result = it.data?.let { intent -> Auth.GoogleSignInApi.getSignInResultFromIntent(intent) } ?: return@registerForActivityResult
        if (result.isSuccess) {
            signInWithGoogleAccount(result.signInAccount)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            btnLoginEmail.setOnClickListener { signUpWithEmail() }
            btnLoginGoogle.setOnClickListener { googleLogin() }
            btnLoginFacebook.setOnClickListener { facebookLogin() }
        }
    }

    /**
     * email 로그인 시 먼저 signUp을 시도하고 이미 있는 유저면 signIn
     */
    private fun signUpWithEmail() {
        FbAuth()
            .createUserWithEmailAndPassword(binding.etEmail.text.toString(), binding.etPassword.text.toString())
            .addOnCompleteListener { task ->
                when {
                    task.isSuccessful -> moveToMainPage(task.result.user)
                    task.exception is FirebaseAuthUserCollisionException -> signInWithEmail()
                    task.exception is Exception -> {
                        Log.w(TAG, "signInAndSignUp: Failure", task.exception)
                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
    }

    /**
     * Facebook 계정에 로그인해서 계정 정보 받아옴
     */
    private fun facebookLogin() {
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("public_profile", "email"))
        LoginManager.getInstance().registerCallback(fbCallbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult?) {
                signInWithFacebookAccount(result?.accessToken)
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

    /**
     * 입력한 이메일과 비밀번호로 Firebase 로그인 
     */
    private fun signInWithEmail() {
        FbAuth()
            .signInWithEmailAndPassword(binding.etEmail.text.toString(), binding.etPassword.text.toString())
            .addOnCompleteListener(onCompleteListener)
    }

    /**
     * Facebook 계정 정보로 Firebase 로그인
     */
    private fun signInWithFacebookAccount(token: AccessToken?) = kotlin.runCatching {
        val credential = FacebookAuthProvider.getCredential(token?.token!!)
        FbAuth()
            .signInWithCredential(credential)
            .addOnCompleteListener(onCompleteListener)
    }

    /**
     * Google 계정 정보로 Firebase 로그인
     */
    private fun signInWithGoogleAccount(account: GoogleSignInAccount?) = kotlin.runCatching {
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        FbAuth()
            .signInWithCredential(credential)
            .addOnCompleteListener(onCompleteListener)
    }

    private fun moveToMainPage(user: FirebaseUser?) {
        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}