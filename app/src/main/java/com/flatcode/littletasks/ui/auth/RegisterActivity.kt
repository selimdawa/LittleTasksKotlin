package com.flatcode.littletasks.ui.auth

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littletasks.core.utils.CLASS
import com.flatcode.littletasks.core.utils.VOID
import com.flatcode.littletasks.databinding.ActivityRegisterBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private var _binding: ActivityRegisterBinding? = null
    private val binding get() = _binding!!

    private val context: Context = this@RegisterActivity
    private val viewModel: AuthViewModel by viewModels()
    private var dialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dialog = ProgressDialog(this).apply {
            setTitle("Please wait...")
            setCanceledOnTouchOutside(false)
        }

        binding.login.setOnClickListener {
            VOID.Intent1(context, CLASS.LOGIN)
            finish()
        }
        binding.forget.setOnClickListener { VOID.Intent1(context, CLASS.FORGET_PASSWORD) }
        binding.go.setOnClickListener { validateData() }

        viewModel.authResult.observe(this) { result ->
            dialog?.dismiss()
            result.onSuccess {
                Toast.makeText(context, "Account created...", Toast.LENGTH_SHORT).show()
                VOID.IntentClear(context, CLASS.MAIN)
                finish()
            }.onFailure { e ->
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateData() {
        val name = binding.nameEt.text.toString().trim()
        val email = binding.emailEt.text.toString().trim()
        val password = binding.passwordEt.text.toString().trim()
        val cPassword = binding.cPasswordEt.text.toString().trim()

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(context, "Enter you name...", Toast.LENGTH_SHORT).show()
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(context, "Invalid email pattern...!", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(context, "Enter password...!", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(cPassword)) {
            Toast.makeText(context, "Confirm Password...!", Toast.LENGTH_SHORT).show()
        } else if (password != cPassword) {
            Toast.makeText(context, "Password doesn't match...!", Toast.LENGTH_SHORT).show()
        } else {
            dialog?.setMessage("Creating account...")
            dialog?.show()
            viewModel.register(name, email, password)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}