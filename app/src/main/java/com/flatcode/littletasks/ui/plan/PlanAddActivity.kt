package com.flatcode.littletasks.ui.plan

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
import com.flatcode.littletasks.databinding.ActivityPlanAddBinding
import com.flatcode.littletasks.databinding.LayoutLoadingDialogBinding
import com.flatcode.littletasks.utils.BaseActivity
import com.flatcode.littletasks.utils.startCropActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PlanAddActivity : BaseActivity() {

    private var _binding: ActivityPlanAddBinding? = null
    private val binding get() = _binding!!

    private val context: Context = this@PlanAddActivity
    private var imageUri: Uri? = null
    private var progressDialog: AlertDialog? = null
    private val viewModel: PlanViewModel by viewModels()

    private val cropImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                imageUri = result.data?.let { intent ->
                    IntentCompat.getParcelableExtra(intent, "CROP_RESULT_URI", Uri::class.java)
                }
                binding.image.setImageURI(null)
                binding.image.setImageURI(imageUri)
            }
        }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                cropImageLauncher.launch(context.startCropActivity(it, 2, 1, false))
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

        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            pickImageLauncher.launch("image/*")
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityPlanAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.nameSpace.setText(R.string.add_new_plan)
        binding.toolbar.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.editImage.setOnClickListener {
            checkPermissionAndPickImage()
        }
        binding.toolbar.ok.setOnClickListener { validateData() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.actionResult.collectLatest { result ->
                    result?.let {
                        dismissLoading()
                        it.onSuccess {
                            Toast.makeText(context, "Successfully uploaded...", Toast.LENGTH_SHORT)
                                .show()
                            finish()
                        }.onFailure { e ->
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }
        }
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
        val title = binding.planEt.text.toString().trim()

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(context, "Enter Title...", Toast.LENGTH_SHORT).show()
        } else if (imageUri == null) {
            Toast.makeText(context, "Pick Image...", Toast.LENGTH_SHORT).show()
        } else {
            showLoading()
            viewModel.addPlan(title, imageUri!!)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissLoading()
        _binding = null
    }
}