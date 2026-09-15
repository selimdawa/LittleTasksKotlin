package com.flatcode.littletasks.ui.plan

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littletasks.R
import com.flatcode.littletasks.core.utils.getFileExtension
import com.flatcode.littletasks.databinding.ActivityPlanAddBinding
import com.theartofdev.edmodo.cropper.CropImage
import dagger.hilt.android.AndroidEntryPoint

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PlanAddActivity : AppCompatActivity() {

    private var _binding: ActivityPlanAddBinding? = null
    private val binding get() = _binding!!

    private val context: Context = this@PlanAddActivity
    private var imageUri: Uri? = null
    private var progressDialog: AlertDialog? = null
    private val viewModel: PlanViewModel by viewModels()

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
                binding.image.setImageURI(imageUri)
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
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        _binding = ActivityPlanAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.nameSpace.setText(R.string.add_new_plan)
        binding.toolbar.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.editImage.setOnClickListener {
            pickImageLauncher.launch(CropImage.getPickImageChooserIntent(this))
        }
        binding.toolbar.ok.setOnClickListener { validateData() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.actionResult.collectLatest { result ->
                    result?.let {
                        dismissLoading()
                        it.onSuccess {
                            Toast.makeText(context, "Successfully uploaded...", Toast.LENGTH_SHORT).show()
                            finish()
                        }.onFailure { e ->
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
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
        val title = binding.planEt.text.toString().trim()

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(context, "Enter Title...", Toast.LENGTH_SHORT).show()
        } else if (imageUri == null) {
            Toast.makeText(context, "Pick Image...", Toast.LENGTH_SHORT).show()
        } else {
            showLoading()
            val ext = imageUri?.getFileExtension(context) ?: "jpg"
            viewModel.addPlan(title, imageUri!!, ext)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissLoading()
        _binding = null
    }
}
