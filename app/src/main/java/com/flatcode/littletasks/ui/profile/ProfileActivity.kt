package com.flatcode.littletasks.ui.profile

import android.content.Context
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littletasks.databinding.ActivityProfileBinding
import com.flatcode.littletasks.utils.BaseActivity
import com.flatcode.littletasks.utils.DATA
import com.flatcode.littletasks.utils.loadImage
import com.flatcode.littletasks.utils.openActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileActivity : BaseActivity() {

    private var _binding: ActivityProfileBinding? = null
    private val binding get() = _binding!!

    private val context: Context = this@ProfileActivity
    private var profileId: String? = null
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        profileId = intent.getStringExtra(DATA.PROFILE_ID)

        binding.edit.setOnClickListener { context.openActivity<ProfileEditActivity>() }
        binding.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userInfo.collectLatest { item ->
                    item?.let {
                        binding.username.text = it.username
                        binding.profile.loadImage(true, it.profileImage)
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.nrTasks.collectLatest { binding.numberTasks.text = it.toString() }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.nrPlans.collectLatest { binding.numberPlans.text = it.toString() }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.nrObjects.collectLatest { binding.numberObjects.text = it.toString() }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.nrCategories.collectLatest {
                    binding.numberCategories.text = it.toString()
                }
            }
        }
    }

    private fun init() {
        val id = profileId ?: return
        viewModel.loadUserInfo(id)
        viewModel.getNrItems(DATA.TASKS, id)
        viewModel.getNrItems(DATA.PLANS, id)
        viewModel.getNrItems(DATA.OBJECTS, id)
        viewModel.getNrItems(DATA.CATEGORIES, id)
    }

    override fun onResume() {
        super.onResume()
        init()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
