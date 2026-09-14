package com.flatcode.littletasks.ui.settings

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littletasks.R
import com.flatcode.littletasks.databinding.ActivityPrivacyPolicyBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PrivacyPolicyActivity : AppCompatActivity() {

    private var _binding: ActivityPrivacyPolicyBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityPrivacyPolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.nameSpace.setText(R.string.privacy_policy)
        binding.toolbar.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.toolbar.search.visibility = View.GONE

        viewModel.privacyPolicy.observe(this) { text ->
            binding.text.text = text
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadPrivacyPolicy()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
