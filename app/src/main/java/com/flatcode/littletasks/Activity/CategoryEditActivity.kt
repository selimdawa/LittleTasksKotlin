package com.flatcode.littletasks.Activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littletasks.Model.Category
import com.flatcode.littletasks.Model.Plan
import com.flatcode.littletasks.R
import com.flatcode.littletasks.Unit.DATA
import com.flatcode.littletasks.Unit.THEME
import com.flatcode.littletasks.Unit.VOID
import com.flatcode.littletasks.databinding.ActivityCategoryAddBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.theartofdev.edmodo.cropper.CropImage

class CategoryEditActivity : AppCompatActivity() {

    private var _binding: ActivityCategoryAddBinding? = null
    private val binding get() = _binding!!

    private val context: Context = this@CategoryEditActivity
    private var categoryId: String? = null
    private var planId: String? = null
    private var imageUri: Uri? = null
    private var progressDialog: AlertDialog? = null
    private var name = DATA.EMPTY

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
        THEME.setThemeOfApp(context)
        super.onCreate(savedInstanceState)
        _binding = ActivityCategoryAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        categoryId = intent.getStringExtra(DATA.CATEGORY_ID)
        planId = intent.getStringExtra(DATA.PLAN_ID)

        loadCategoryInfo()
        loadPlanInfo()

        binding.toolbar.nameSpace.setText(R.string.edit_category)
        binding.toolbar.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.editImage.setOnClickListener {
            pickImageLauncher.launch(CropImage.getPickImageChooserIntent(this))
        }
        binding.toolbar.ok.setOnClickListener { validateData() }
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
        name = binding.categoryEt.text.toString().trim()
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(context, "Enter name...", Toast.LENGTH_SHORT).show()
        } else {
            if (imageUri == null) {
                updateCategory(DATA.EMPTY)
            } else {
                uploadImage()
            }
        }
    }

    private fun uploadImage() {
        showLoading()
        val filePathAndName = "Images/Category/$categoryId"
        val reference = FirebaseStorage.getInstance()
            .getReference(filePathAndName + DATA.DOT + VOID.getFileExtension(imageUri, context))

        reference.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    updateCategory(uri.toString())
                }.addOnFailureListener { e ->
                    dismissLoading()
                    Toast.makeText(
                        context,
                        "Failed to get download URL: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }.addOnFailureListener { e ->
                dismissLoading()
                Toast.makeText(
                    context,
                    "Failed to upload image due to ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun updateCategory(imageUrl: String?) {
        val catId = categoryId ?: return
        showLoading()

        val hashMap = HashMap<String, Any>().apply {
            put(DATA.NAME, name)
            if (imageUri != null && imageUrl != null) {
                put(DATA.IMAGE, imageUrl)
            }
        }

        FirebaseDatabase.getInstance().getReference(DATA.CATEGORIES)
            .child(catId)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                dismissLoading()
                Toast.makeText(context, "Category updated...", Toast.LENGTH_SHORT).show()
                finish()
            }.addOnFailureListener { e ->
                dismissLoading()
                Toast.makeText(
                    context,
                    "Failed to update db due to ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun loadCategoryInfo() {
        val catId = categoryId ?: return
        FirebaseDatabase.getInstance().getReference(DATA.CATEGORIES).child(catId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val item = snapshot.getValue(Category::class.java) ?: return
                    val categoryName = item.name
                    val image = item.image

                    _binding?.let { b ->
                        VOID.GlideImage(true, context, image, b.image)
                        b.categoryEt.setText(categoryName)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun loadPlanInfo() {
        val pId = planId ?: return
        FirebaseDatabase.getInstance().getReference(DATA.PLANS).child(pId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val item = snapshot.getValue(Plan::class.java) ?: return
                    _binding?.plan?.text = item.name
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