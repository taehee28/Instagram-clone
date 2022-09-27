package com.thk.instagram_clone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.thk.instagram_clone.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private val TAG = LoginActivity::class.simpleName
    private lateinit var binding: ActivityLoginBinding

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            btnLoginEmail.setOnClickListener { signInAndSignUp() }
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

    private fun moveToMainPage(user: FirebaseUser?) {
        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}