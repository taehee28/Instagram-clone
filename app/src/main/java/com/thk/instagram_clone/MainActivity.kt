package com.thk.instagram_clone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.messaging.FirebaseMessaging
import com.thk.instagram_clone.databinding.ActivityMainBinding
import com.thk.data.util.Firebase
import com.thk.instagram_clone.viewmodel.DetailViewViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var controller: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        registerPushToken()

        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.container) as NavHostFragment
        controller = navHostFragment.navController
        setBottomNav()
        setupToolbar()
    }

    override fun onResume() {
        super.onResume()
        controller.addOnDestinationChangedListener(listener)
    }

    override fun onPause() {
        controller.removeOnDestinationChangedListener(listener)
        super.onPause()
    }

    private fun registerPushToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result ?: ""
                val uid = Firebase.auth.currentUser?.uid ?: return@addOnCompleteListener

                Firebase.firestore
                    .collection("pushtokens")
                    .document(uid)
                    .set(mapOf("pushToken" to token))
            }
        }
    }

    private fun setBottomNav() {
        binding.bottomNavigation.setupWithNavController(controller)
    }

    private fun setupToolbar() {
        val appBarConfig = AppBarConfiguration(setOf(R.id.detailViewFragment, R.id.alarmFragment, R.id.accountFragment, R.id.gridFragment))
        binding.toolbar.setupWithNavController(controller, appBarConfig)
    }

    private val listener = NavController.OnDestinationChangedListener {_, destination, _ ->
        if (destination.id == R.id.profileViewFragment) {
            supportActionBar?.setLogo(null)
            supportActionBar?.title = ""
            supportActionBar?.subtitle = ""
        } else {
            supportActionBar?.setLogo(R.drawable.logo_title)
        }
    }
}