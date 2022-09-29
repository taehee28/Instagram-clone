package com.thk.instagram_clone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.navigation.NavigationBarView.OnItemSelectedListener
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener
import com.thk.instagram_clone.databinding.ActivityMainBinding
import com.thk.instagram_clone.navigation.AccountFragment
import com.thk.instagram_clone.navigation.AlarmFragment
import com.thk.instagram_clone.navigation.DetailViewFragment
import com.thk.instagram_clone.navigation.GridFragment

class MainActivity : AppCompatActivity(), OnItemSelectedListener {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            viewPager.apply {
                adapter = MainViewPager(this@MainActivity)
                isUserInputEnabled = false
                offscreenPageLimit = 3
            }

            bottomNavigation.setOnItemSelectedListener(this@MainActivity)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_home -> {
                binding.viewPager.setCurrentItem(0, false)
                true
            }
            R.id.action_search -> {
                binding.viewPager.setCurrentItem(1, false)
                true
            }
            R.id.action_add_photo -> {
                startActivity(Intent(this, AddPhotoActivity::class.java))
                true
            }
            R.id.action_favorite_alarm -> {
                binding.viewPager.setCurrentItem(2, false)
                true
            }
            R.id.action_account -> {
                binding.viewPager.setCurrentItem(3, false)
                true
            }
            else -> false
        }
    }
}

class MainViewPager(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount() = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DetailViewFragment.newInstance()
            1 -> GridFragment.newInstance()
            2 -> AlarmFragment.newInstance()
            3 -> AccountFragment.newInstance()
            else -> throw IllegalArgumentException()
        }
    }
}