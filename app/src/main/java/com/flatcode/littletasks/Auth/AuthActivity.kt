package com.flatcode.littletasks.Auth

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littletasks.Unit.CLASS
import com.flatcode.littletasks.Unit.THEME
import com.flatcode.littletasks.Unit.VOID
import com.flatcode.littletasks.databinding.ActivityAuthBinding

class AuthActivity : AppCompatActivity() {

    var context: Context = this@AuthActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        THEME.setThemeOfApp(context)
        super.onCreate(savedInstanceState)
        val binding = ActivityAuthBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        VOID.Logo(baseContext, binding.logo)
        VOID.Intro(baseContext, binding.background, binding.backWhite, binding.backBlack)

        binding.loginBtn.setOnClickListener { VOID.Intent1(context, CLASS.LOGIN) }
        binding.skipBtn.setOnClickListener { VOID.Intent1(context, CLASS.REGISTER) }
    }
}