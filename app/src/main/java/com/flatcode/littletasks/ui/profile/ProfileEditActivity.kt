package com.flatcode.littletasks.ui.profile

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littletasks.R
import com.flatcode.littletasks.core.utils.DATA
import com.flatcode.littletasks.core.utils.VOID
import com.flatcode.littletasks.databinding.ActivityProfileEditBinding
import com.theartofdev.edmodo.cropper.CropImage
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileEditActivity : AppCompatActivity() {

    private var _binding: ActivityProfileEditBinding? = null
    private val binding get() = _binding!!

    private val context: Context = this@ProfileEditActivity
    private var imageUri: Uri? = null
    private var progressDialog: AlertDialog? = null
    private val viewModel: ProfileViewModel by viewModels()

    private val cropImageLauncher = registerForActivityResult(
        object : ActivityResultContract<Intent?, CropImage.ActivityResult?>() {
            override fun createIntent(context: Context, input: Intent?): Intent {
                return input ?: CropImage.activity().getIntent(context)
            }
            override fun parseResult(resultCode: Int, intent: Intent?): CropImage.ActivityResult? {
                return if (intent != null) CropImage.getActivityResult(intent) else null
            }
        }
    ) { result ->
        if (result != null) {
            if (result.error == null) {
                imageUri = result.uri
                binding.profileImage.setImageURI(imageUri)
            } else {
                Toast.makeText(this, "Error! ${result.error}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri = CropImage.getPickImageResultUri(context, result.data)
            if (CropImage.isReadExternalStoragePermissionsRequired(context, uri)) {
                imageUri = uri
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            } else {
                cropImageLauncher.launch(CropImage.activity(uri).getIntent(this))
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cropImageLauncher.launch(CropImage.activity(imageUri).getIntent(this))
        } else {
            Toast.makeText(context, "Permission denied...", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.nameSpace.setText(R.string.edit_profile)
        binding.toolbar.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.image.setOnClickListener {
            pickImageLauncher.launch(CropImage.getPickImageChooserIntent(this))
        }
        binding.go.setOnClickListener { validateData() }

        viewModel.userInfo.observe(this) { user ->
            binding.nameEt.setText(user.username)
            VOID.GlideImage(true, context, user.profileImage, binding.profileImage)
        }

        viewModel.actionResult.observe(this) { result ->
            dismissLoading()
            result.onSuccess {
                Toast.makeText(context, "Profile updated...", Toast.LENGTH_SHORT).show()
                finish()
            }.onFailure { e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.loadUserInfo(DATA.FirebaseUserUid)
    }

    private fun showLoading() {
        if (progressDialog == null) {
            progressDialog = AlertDialog.Builder(context)
                .setView(R.layout.layout_loading_dialog)
                .setCancelable(false)
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
            val extension = imageUri?.let { VOID.getFileExtension(it, context) }
            viewModel.updateProfile(username, imageUri, extension)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissLoading()
        _binding = null
    }
}
