package com.flatcode.littletasks.ui.auth

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littletasks.databinding.ActivityLoginBinding
import com.flatcode.littletasks.databinding.LayoutLoadingDialogBinding
import com.flatcode.littletasks.ui.main.MainActivity
import com.flatcode.littletasks.utils.openActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!

    private val context: Context = this@LoginActivity
    private val viewModel: AuthViewModel by viewModels()
    private var dialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val loadingBinding = LayoutLoadingDialogBinding.inflate(layoutInflater)
        dialog =
            AlertDialog.Builder(this).setView(loadingBinding.root).setCancelable(false).create()

        binding.forget.setOnClickListener { context.openActivity<ForgetPasswordActivity>() }
        binding.noAccount.setOnClickListener { context.openActivity<RegisterActivity>() }
        binding.loginBtn.setOnClickListener { validateData() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.authResult.collectLatest { result ->
                    result?.let {
                        dialog?.dismiss()
                        it.onSuccess {
                            context.openActivity<MainActivity>(true)
                        }.onFailure { e ->
                            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun validateData() {
        val email = binding.emailEt.text.toString().trim()
        val password = binding.passwordEt.text.toString().trim()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(context, "Invalid email pattern...!", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(context, "Enter password...!", Toast.LENGTH_SHORT).show()
        } else {
            dialog?.setMessage("Logging In...")
            dialog?.show()
            viewModel.login(email, password)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}