package com.flatcode.littletasks.ui.category

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littletasks.R
import com.flatcode.littletasks.databinding.ActivityCategoryAddBinding
import com.flatcode.littletasks.databinding.LayoutLoadingDialogBinding
import com.flatcode.littletasks.utils.DATA
import com.flatcode.littletasks.utils.startCropActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CategoryAddActivity : AppCompatActivity() {

    private var _binding: ActivityCategoryAddBinding? = null
    private val binding get() = _binding!!

    private val context: Context = this@CategoryAddActivity
    private var imageUri: Uri? = null
    private var progressDialog: AlertDialog? = null
    private var planId: String? = null
    private val viewModel: CategoryViewModel by viewModels()

    private val cropImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                imageUri = result.data?.getParcelableExtra("CROP_RESULT_URI")
                binding.image.setImageURI(null)
                binding.image.setImageURI(imageUri)
            }
        }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                cropImageLauncher.launch(context.startCropActivity(it, 1, 1, false))
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        _binding = ActivityCategoryAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        planId = intent.getStringExtra(DATA.ID)

        binding.toolbar.nameSpace.setText(R.string.add_new_category)
        binding.toolbar.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.editImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
        binding.toolbar.ok.setOnClickListener { validateData() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.planName.collectLatest { name ->
                    binding.plan.text = name
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uploadResult.collectLatest { result ->
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

        planId?.let { viewModel.loadPlanName(it) }
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
        val title = binding.categoryEt.text.toString().trim()

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(context, "Enter Title...", Toast.LENGTH_SHORT).show()
        } else if (imageUri == null) {
            Toast.makeText(context, "Pick Image...", Toast.LENGTH_SHORT).show()
        } else {
            showLoading()
            viewModel.addCategory(title, planId ?: "", imageUri!!)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissLoading()
        _binding = null
    }
}
