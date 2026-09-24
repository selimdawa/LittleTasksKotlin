package com.flatcode.littletasks.ui.profile

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littletasks.R
import com.flatcode.littletasks.databinding.ActivityProfileEditBinding
import com.flatcode.littletasks.databinding.LayoutLoadingDialogBinding
import com.flatcode.littletasks.utils.BaseActivity
import com.flatcode.littletasks.utils.DATA
import com.flatcode.littletasks.utils.loadImage
import com.flatcode.littletasks.utils.startCropActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileEditActivity : BaseActivity() {

    private var _binding: ActivityProfileEditBinding? = null
    private val binding get() = _binding!!

    private val context: Context = this@ProfileEditActivity
    private var imageUri: Uri? = null
    private var progressDialog: AlertDialog? = null
    private val viewModel: ProfileViewModel by viewModels()
    private var isLoaded = false

    private val cropImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                imageUri = result.data?.let { intent ->
                    IntentCompat.getParcelableExtra(intent, "CROP_RESULT_URI", Uri::class.java)
                }
                binding.profileImage.setImageURI(null)
                binding.profileImage.setImageURI(imageUri)
            }
        }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                cropImageLauncher.launch(context.startCropActivity(it, 1, 1, true))
            }
        }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                pickImageLauncher.launch("image/*")
            } else {
                Toast.makeText(context, "Permission denied...", Toast.LENGTH_SHORT).show()
            }
        }

    private fun checkPermissionAndPickImage() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(
                context, permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            pickImageLauncher.launch("image/*")
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.nameSpace.setText(R.string.edit_profile)
        binding.toolbar.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.image.setOnClickListener {
            checkPermissionAndPickImage()
        }
        binding.go.setOnClickListener { validateData() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userInfo.collectLatest { user ->
                    user?.let {
                        if (!isLoaded) {
                            binding.nameEt.setText(it.username)
                            binding.profileImage.loadImage(true, it.profileImage)
                            isLoaded = true
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.actionResult.collectLatest { result ->
                    result?.let {
                        dismissLoading()
                        it.onSuccess {
                            Toast.makeText(context, "Profile updated...", Toast.LENGTH_SHORT).show()
                            finish()
                        }.onFailure { e ->
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }
        }

        viewModel.loadUserInfo(DATA.firebaseUserUid)
    }

    private fun showLoading() {
        if (progressDialog == null) {
            val loadingBinding = LayoutLoadingDialogBinding.inflate(layoutInflater)
            progressDialog =
                AlertDialog.Builder(context).setView(loadingBinding.root).setCancelable(false)
                    .create()
        }
        progressDialog?.show()
    }

    private fun dismissLoading() {
        progressDialog?.dismiss()
    }

    private fun validateData() {
        val username = binding.nameEt.text.toString().trim()
        if (TextUtils.isEmpty(username)) {
            Toast.makeText(context, "Enter name...", Toast.LENGTH_SHORT).show()
        } else {
            showLoading()
            viewModel.updateProfile(username, imageUri)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissLoading()
        _binding = null
    }
}