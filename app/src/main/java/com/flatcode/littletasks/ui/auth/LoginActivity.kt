package com.flatcode.littletasks.ui.auth

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littletasks.core.utils.openActivity
import com.flatcode.littletasks.core.utils.openActivityAndClear
import com.flatcode.littletasks.databinding.ActivityLoginBinding
import com.flatcode.littletasks.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!

    private val context: Context = this@LoginActivity
    private val viewModel: AuthViewModel by viewModels()
    private var dialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dialog = ProgressDialog(this).apply {
            setTitle("Please wait...")
            setCanceledOnTouchOutside(false)
        }

        binding.forget.setOnClickListener { context.openActivity(ForgetPasswordActivity::class.java) }
        binding.noAccount.setOnClickListener { context.openActivity(RegisterActivity::class.java) }
        binding.loginBtn.setOnClickListener { validateDate() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.authResult.collectLatest { result ->
                    result?.let {
                        dialog?.dismiss()
                        it.onSuccess {
                            context.openActivityAndClear(MainActivity::class.java)
                        }.onFailure { e ->
                            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun validateDate() {
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