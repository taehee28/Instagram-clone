package com.thk.instagram_clone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.thk.instagram_clone.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var controller: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

    private fun setBottomNav() {
        binding.bottomNavigation.setupWithNavController(controller)
    }

    private fun setupToolbar() {
        val appBarConfig = AppBarConfiguration(setOf(R.id.detailViewFragment, R.id.alarmFragment, R.id.accountFragment))
        binding.toolbar.setupWithNavController(controller, appBarConfig)
    }

    private val listener = NavController.OnDestinationChangedListener {controller, destination, arguments ->
        // TODO: 로고를 없애고 subtitle을 보여주기
    }
}