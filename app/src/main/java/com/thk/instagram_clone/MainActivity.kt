package com.thk.instagram_clone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.navigation.NavigationBarView.OnItemSelectedListener
import com.thk.instagram_clone.databinding.ActivityMainBinding
import com.thk.instagram_clone.navigation.AccountFragment
import com.thk.instagram_clone.navigation.AlarmFragment
import com.thk.instagram_clone.navigation.DetailViewFragment
import com.thk.instagram_clone.navigation.GridFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setBottomNav()
    }

    private fun setBottomNav() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.container) as NavHostFragment
        val controller = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(controller)
    }
}