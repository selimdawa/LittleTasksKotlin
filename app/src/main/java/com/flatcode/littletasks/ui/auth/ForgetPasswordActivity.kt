package com.flatcode.littletasks.ui.auth

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littletasks.core.utils.CLASS
import com.flatcode.littletasks.core.utils.VOID
import com.flatcode.littletasks.databinding.ActivityForgetPasswordBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ForgetPasswordActivity : AppCompatActivity() {

    private var _binding: ActivityForgetPasswordBinding? = null
    private val binding get() = _binding!!

    private val context: Context = this@ForgetPasswordActivity
    private val viewModel: AuthViewModel by viewModels()
    private var dialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityForgetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dialog = ProgressDialog(this).apply {
            setTitle("Please wait...")
            setCanceledOnTouchOutside(false)
        }

        binding.noAccount.setOnClickListener {
            VOID.Intent1(context, CLASS.REGISTER)
            finish()
        }
        binding.login.setOnClickListener {
            VOID.Intent1(context, CLASS.LOGIN)
            finish()
        }
        binding.go.setOnClickListener { validateDate() }

        viewModel.authResult.observe(this) { result ->
            dialog?.dismiss()
            result.onSuccess {
                Toast.makeText(
                    context, "Instructions to reset password sent", Toast.LENGTH_SHORT
                ).show()
            }.onFailure { e ->
                Toast.makeText(context, "Failed to send: " + e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateDate() {
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