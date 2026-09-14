package com.flatcode.littletasks.ui.profile

import android.content.Context
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littletasks.core.utils.CLASS
import com.flatcode.littletasks.core.utils.DATA
import com.flatcode.littletasks.core.utils.VOID
import com.flatcode.littletasks.databinding.ActivityProfileBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {

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

        binding.edit.setOnClickListener { VOID.Intent1(context, CLASS.PROFILE_EDIT) }
        binding.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        viewModel.userInfo.observe(this) { item ->
            binding.username.text = item.username
            VOID.GlideImage(true, context, item.profileImage, binding.profile)
        }

        viewModel.nrTasks.observe(this) { binding.numberTasks.text = it.toString() }
        viewModel.nrPlans.observe(this) { binding.numberPlans.text = it.toString() }
        viewModel.nrObjects.observe(this) { binding.numberObjects.text = it.toString() }
        viewModel.nrCategories.observe(this) { binding.numberCategories.text = it.toString() }
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
