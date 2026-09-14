package com.flatcode.littletasks.ui.auth

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littletasks.core.utils.CLASS
import com.flatcode.littletasks.core.utils.VOID
import com.flatcode.littletasks.databinding.ActivityAuthBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    private val context: Context = this@AuthActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginBtn.setOnClickListener { VOID.Intent1(context, CLASS.LOGIN) }
        binding.skipBtn.setOnClickListener { VOID.Intent1(context, CLASS.REGISTER) }
    }
}