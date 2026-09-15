package com.flatcode.littletasks.ui.auth

import android.content.Context
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littletasks.core.utils.openActivity
import com.flatcode.littletasks.databinding.ActivityAuthBinding
import com.flatcode.littletasks.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    private val context: Context = this@AuthActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginBtn.setOnClickListener { context.openActivity(LoginActivity::class.java) }
        binding.skipBtn.setOnClickListener { context.openActivity(RegisterActivity::class.java) }
    }
}