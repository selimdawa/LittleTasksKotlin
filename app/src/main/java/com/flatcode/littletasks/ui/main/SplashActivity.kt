package com.flatcode.littletasks.ui.main

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littletasks.core.utils.openActivity
import com.flatcode.littletasks.databinding.ActivitySplashBinding
import com.flatcode.littletasks.ui.auth.AuthActivity
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private var _binding: ActivitySplashBinding? = null
    private val binding get() = _binding!!

    private val context: Context = this@SplashActivity
    
    @Inject
    lateinit var auth: FirebaseAuth

    private val timePerSecond = 2
    private val timeFinal = TIME_PER_MILLIS * timePerSecond

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        _binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler(Looper.getMainLooper()).postDelayed({ checkUser() }, timeFinal.toLong())
    }

    private fun checkUser() {
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            context.openActivity(AuthActivity::class.java)
        } else {
            context.openActivity(MainActivity::class.java)
        }
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        const val TIME_PER_MILLIS = 1000
    }
}