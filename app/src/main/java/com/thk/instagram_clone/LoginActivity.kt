package com.thk.instagram_clone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.thk.instagram_clone.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private val TAG = LoginActivity::class.simpleName
    private lateinit var binding: ActivityLoginBinding

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        GoogleSignIn.getClient(this, gso)
    }

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
            btnLoginEmail.setOnClickListener { signInAndSignUp() }
            btnLoginGoogle.setOnClickListener { googleLogin() }
        }
    }

    private fun signInAndSignUp() {
        auth
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

    private fun signInWithEmail() {
        auth
            .signInWithEmailAndPassword(binding.etEmail.text.toString(), binding.etPassword.text.toString())
            .addOnCompleteListener { task ->
                when {
                    task.isSuccessful -> moveToMainPage(task.result.user)
                    else -> {
                        Log.w(TAG, "signInAndSignUp: Failure", task.exception)
                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
    }

    private fun googleLogin() = googleLoginLauncher.launch(googleSignInClient.signInIntent)

    private fun signInWithGoogleAccount(account: GoogleSignInAccount?) {
        if (account == null) return

        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth
            .signInWithCredential(credential)
            .addOnCompleteListener { task ->
                when {
                    task.isSuccessful -> moveToMainPage(task.result.user)
                    else -> {
                        Log.w(TAG, "signInAndSignUp: Failure", task.exception)
                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
    }

    private fun moveToMainPage(user: FirebaseUser?) {
        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}