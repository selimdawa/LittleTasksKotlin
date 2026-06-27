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
import com.flatcode.littletasks.Model.Plan
import com.flatcode.littletasks.Model.TaskItem
import com.flatcode.littletasks.R
import com.flatcode.littletasks.Unit.DATA
import com.flatcode.littletasks.Unit.THEME
import com.flatcode.littletasks.Unit.VOID
import com.flatcode.littletasks.databinding.ActivityCategoryAddBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.theartofdev.edmodo.cropper.CropImage

class CategoryAddActivity : AppCompatActivity() {

    private var _binding: ActivityCategoryAddBinding? = null
    private val binding get() = _binding!!

    private val context: Context = this@CategoryAddActivity
    private var imageUri: Uri? = null
    private var progressDialog: AlertDialog? = null
    private var planId: String? = null
    private var title = DATA.EMPTY

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

        planId = intent.getStringExtra(DATA.ID)

        binding.toolbar.nameSpace.setText(R.string.add_new_category)
        binding.toolbar.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.editImage.setOnClickListener {
            pickImageLauncher.launch(CropImage.getPickImageChooserIntent(this))
        }
        binding.toolbar.ok.setOnClickListener { validateData() }
        loadPlanName()
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
        title = binding.categoryEt.text.toString().trim()

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(context, "Enter Title...", Toast.LENGTH_SHORT).show()
        } else if (imageUri == null) {
            Toast.makeText(context, "Pick Image...", Toast.LENGTH_SHORT).show()
        } else {
            uploadToStorage()
        }
    }

    private fun loadPlanName() {
        val pId = planId ?: return
        FirebaseDatabase.getInstance().getReference(DATA.PLANS).child(pId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val plan = dataSnapshot.getValue(Plan::class.java) ?: return
                    val planName = DATA.EMPTY + plan.name
                    _binding?.plan?.text = planName
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun uploadToStorage() {
        showLoading()
        val ref = FirebaseDatabase.getInstance().getReference(DATA.CATEGORIES)
        val id = ref.push().key ?: return
        val filePathAndName = "Images/Category/$id"
        val reference = FirebaseStorage.getInstance()
            .getReference(filePathAndName + DATA.DOT + VOID.getFileExtension(imageUri, context))

        reference.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    uploadInfoDB(uri.toString(), id, ref)
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
                    "Category upload failed due to ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun uploadInfoDB(uploadedImageUrl: String, id: String, ref: DatabaseReference) {
        val hashMap = HashMap<String, Any?>().apply {
            put(DATA.PUBLISHER, DATA.EMPTY + DATA.FirebaseUserUid)
            put(DATA.TIMESTAMP, System.currentTimeMillis())
            put(DATA.ID, id)
            put(DATA.NAME, DATA.EMPTY + title)
            put(DATA.PLAN, DATA.EMPTY + planId)
            put(DATA.IMAGE, uploadedImageUrl)
        }

        ref.child(id).setValue(hashMap).addOnSuccessListener {
            dismissLoading()
            getItems(id)
            Toast.makeText(context, "Successfully uploaded...", Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener { e ->
            dismissLoading()
            Toast.makeText(
                context,
                "Failure to upload to db due to : ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun getItems(categoryId: String) {
        val uid = DATA.FirebaseUserUid
        FirebaseDatabase.getInstance().getReference(DATA.OBJECTS)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (data in dataSnapshot.children) {
                        val taskItem = data.getValue(TaskItem::class.java) ?: continue
                        if (taskItem.publisher == uid) {
                            checkObject(taskItem.id, categoryId, taskItem.name, taskItem.points)
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    private fun checkObject(objectId: String?, categoryId: String, name: String?, points: Int) {
        val pId = planId ?: return
        val objId = objectId ?: return
        FirebaseDatabase.getInstance().getReference(DATA.PLANS)
            .child(pId).child(DATA.AUTO_TASKS)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.child(objId).exists()) {
                        addAutoTasks(name, points, categoryId)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    private fun addAutoTasks(name: String?, point: Int, categoryId: String) {
        val ref = FirebaseDatabase.getInstance().getReference(DATA.TASKS)
        val id = ref.push().key ?: return
        val hashMap = HashMap<String, Any?>().apply {
            put(DATA.PUBLISHER, DATA.EMPTY + DATA.FirebaseUserUid)
            put(DATA.ID, id)
            put(DATA.NAME, DATA.EMPTY + name)
            put(DATA.POINTS, point)
            put(DATA.AVAILABLE_POINTS, DATA.ZERO)
            put(DATA.RANK, DATA.ZERO)
            put(DATA.CATEGORY, categoryId)
            put(DATA.TIMESTAMP, System.currentTimeMillis())
            put(DATA.START, DATA.ZERO)
            put(DATA.END, DATA.ZERO)
        }
        ref.child(id).setValue(hashMap)
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissLoading()
        _binding = null
    }
}