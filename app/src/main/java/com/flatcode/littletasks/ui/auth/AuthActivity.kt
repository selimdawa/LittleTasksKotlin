package com.flatcode.littletasks.ui.auth

import android.content.Context
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littletasks.databinding.ActivityAuthBinding
import com.flatcode.littletasks.utils.openActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    private var _binding: ActivityAuthBinding? = null
    private val binding get() = _binding!!

    private val context: Context = this@AuthActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        _binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginBtn.setOnClickListener { context.openActivity<LoginActivity>() }
        binding.skipBtn.setOnClickListener { context.openActivity<RegisterActivity>() }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}