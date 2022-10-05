package com.thk.instagram_clone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.thk.instagram_clone.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setBottomNav()
        setupToolbar()
    }

    private fun setBottomNav() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.container) as NavHostFragment
        val controller = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(controller)
    }

    private fun setupToolbar() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.container) as NavHostFragment
        val controller = navHostFragment.navController
        val appBarConfig = AppBarConfiguration(setOf(R.id.detailViewFragment, R.id.alarmFragment, R.id.accountFragment))
        binding.toolbar.setupWithNavController(controller, appBarConfig)
    }
}