package com.flatcode.littletasks.ui.main

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.flatcode.littletasks.R
import com.flatcode.littletasks.core.utils.DATA
import com.flatcode.littletasks.core.utils.closeApp
import com.flatcode.littletasks.core.utils.loadImage
import com.flatcode.littletasks.core.utils.openActivity
import com.flatcode.littletasks.databinding.ActivityMainBinding
import com.flatcode.littletasks.ui.profile.ProfileActivity
import dagger.hilt.android.AndroidEntryPoint
import io.selimdawa.bubblebottom.BubbleBottomNavigation

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private var activity: Activity? = null
    private val context: Context = also { activity = it }

    private val viewModel: MainViewModel by viewModels()

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                this@MainActivity.closeApp()
            }
        })

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainer) as NavHostFragment
        navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->
            _binding?.toolbar?.card?.visibility = if (destination.id == R.id.homeFragment) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        binding.bottomNavigation.apply {
            setupAppMenu()
            setOnShowListener { item -> navController.navigate(item.id) }
            show(R.id.homeFragment, true)
        }

        binding.toolbar.image.setOnClickListener {
            context.openActivity(ProfileActivity::class.java, DATA.PROFILE_ID to DATA.FirebaseUserUid)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.profileImage.collectLatest { profileImage ->
                    if (profileImage.isNotEmpty()) {
                        binding.toolbar.image.loadImage(true, profileImage)
                    }
                }
            }
        }
        viewModel.loadUserInfo()
    }

    override fun onDestroy() {
        super.onDestroy()
        activity = null
        _binding = null
    }

    fun BubbleBottomNavigation.setupAppMenu() {
        add(BubbleBottomNavigation.Model(R.id.settingsFragment, R.drawable.ic_settings))
        add(BubbleBottomNavigation.Model(R.id.homeFragment, R.drawable.ic_home))
        add(BubbleBottomNavigation.Model(R.id.categoriesFragment, R.drawable.ic_group))
        show(R.id.homeFragment, true)
    }
}