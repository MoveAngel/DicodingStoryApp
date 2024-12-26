package com.exam.dcgstoryapp.view.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.exam.dcgstoryapp.R
import com.exam.dcgstoryapp.databinding.ActivityMainBinding
import com.exam.dcgstoryapp.view.ViewModelFactory
import com.exam.dcgstoryapp.view.story.profile.ProfileFragment
import com.exam.dcgstoryapp.view.story.add.AddStoryFragment
import com.exam.dcgstoryapp.view.story.list.ListStoryFragment
import com.exam.dcgstoryapp.view.welcome.WelcomeActivity

class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (supportFragmentManager.backStackEntryCount == 0) {
                    finish()
                } else {
                    supportFragmentManager.popBackStack()
                }
            }
        })

        checkSessionAndNavigate()

        setupView()
        setupBottomNavigation()
    }

    private fun checkSessionAndNavigate() {
        viewModel.getSession().observe(this) { user ->
            if (!user.isLogin) {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            } else {
                navigateToFragment(ListStoryFragment(), R.id.menu_home)
            }
        }
    }

    fun navigateToFragment(fragment: Fragment, menuItemId: Int? = null) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(fragment.javaClass.simpleName)
            .commitAllowingStateLoss()

        menuItemId?.let {
            binding.bottomNavigation.selectedItemId = it
        }
    }

    private fun setupView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    navigateToFragment(ListStoryFragment())
                    true
                }
                R.id.menu_add_story -> {
                    navigateToFragment(AddStoryFragment())
                    true
                }
                R.id.menu_profile -> {
                    navigateToFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }
}