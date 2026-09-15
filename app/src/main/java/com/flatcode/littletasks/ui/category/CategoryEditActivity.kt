package com.flatcode.littletasks.ui.category

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
import com.flatcode.littletasks.core.utils.getFileExtension
import com.flatcode.littletasks.core.utils.loadImage
import com.flatcode.littletasks.data.model.Category
import com.flatcode.littletasks.databinding.ActivityCategoryAddBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.theartofdev.edmodo.cropper.CropImage
import dagger.hilt.android.AndroidEntryPoint

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CategoryEditActivity : AppCompatActivity() {

    private var _binding: ActivityCategoryAddBinding? = null
    private val binding get() = _binding!!

    private val context: Context = this@CategoryEditActivity
    private var categoryId: String? = null
    private var planId: String? = null
    private var imageUri: Uri? = null
    private var progressDialog: AlertDialog? = null
    private val viewModel: CategoryViewModel by viewModels()

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
        super.onCreate(savedInstanceState)
        _binding = ActivityCategoryAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        categoryId = intent.getStringExtra(DATA.CATEGORY_ID)
        planId = intent.getStringExtra(DATA.PLAN_ID)

        loadCategoryInfo()
        planId?.let { viewModel.loadPlanName(it) }

        binding.toolbar.nameSpace.setText(R.string.edit_category)
        binding.toolbar.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.editImage.setOnClickListener {
            pickImageLauncher.launch(CropImage.getPickImageChooserIntent(this))
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
                            Toast.makeText(context, "Category updated...", Toast.LENGTH_SHORT).show()
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
        val name = binding.categoryEt.text.toString().trim()
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(context, "Enter name...", Toast.LENGTH_SHORT).show()
        } else {
            showLoading()
            val ext = imageUri?.getFileExtension(context)
            viewModel.updateCategory(categoryId ?: "", name, imageUri, ext)
        }
    }

    private fun loadCategoryInfo() {
        val catId = categoryId ?: return
        FirebaseDatabase.getInstance().getReference(DATA.CATEGORIES).child(catId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val item = snapshot.getValue(Category::class.java) ?: return
                    binding.categoryEt.setText(item.name)
                    binding.image.loadImage(true, item.image)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissLoading()
        _binding = null
    }
}
