package com.flatcode.littletasks.ui.auth

import android.content.Context
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littletasks.R
import com.flatcode.littletasks.core.utils.openActivity
import com.flatcode.littletasks.databinding.ActivityForgetPasswordBinding
import dagger.hilt.android.AndroidEntryPoint

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ForgetPasswordActivity : AppCompatActivity() {

    private var _binding: ActivityForgetPasswordBinding? = null
    private val binding get() = _binding!!

    private val context: Context = this@ForgetPasswordActivity
    private val viewModel: AuthViewModel by viewModels()
    private var dialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        _binding = ActivityForgetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dialog = AlertDialog.Builder(this)
            .setView(R.layout.layout_loading_dialog)
            .setCancelable(false)
            .create()

        binding.noAccount.setOnClickListener {
            context.openActivity(RegisterActivity::class.java)
            finish()
        }
        binding.login.setOnClickListener {
            context.openActivity(LoginActivity::class.java)
            finish()
        }
        binding.go.setOnClickListener { validateData() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.authResult.collectLatest { result ->
                    result?.let {
                        dialog?.dismiss()
                        it.onSuccess {
                            Toast.makeText(
                                context, "Instructions to reset password sent", Toast.LENGTH_SHORT
                            ).show()
                        }.onFailure { e ->
                            Toast.makeText(context, "Failed to send: " + e.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun validateData() {
        val email = binding.emailEt.text.toString().trim()
        if (email.isEmpty()) {
            Toast.makeText(context, "Enter email...!", Toast.LENGTH_SHORT).show()
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(context, "Invalid email format...!", Toast.LENGTH_SHORT).show()
        } else {
            dialog?.setMessage("Sending recovery instructions...")
            dialog?.show()
            viewModel.recoverPassword(email)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}