package com.flatcode.littletasks.Activity

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littletasks.Model.OBJECT
import com.flatcode.littletasks.Model.Plan
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
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage

class CategoryAddActivity : AppCompatActivity() {

    private var binding: ActivityCategoryAddBinding? = null
    var activity: Activity? = null
    var context: Context = also { activity = it }
    private var imageUri: Uri? = null
    private var dialog: ProgressDialog? = null
    var planId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        THEME.setThemeOfApp(context)
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryAddBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        planId = intent.getStringExtra(DATA.ID)

        dialog = ProgressDialog(context)
        dialog!!.setTitle("Please wait...")
        dialog!!.setCanceledOnTouchOutside(false)

        binding!!.toolbar.nameSpace.setText(R.string.add_new_category)
        binding!!.toolbar.back.setOnClickListener { onBackPressed() }
        binding!!.editImage.setOnClickListener { VOID.CropImageSquare(activity) }
        binding!!.toolbar.ok.setOnClickListener { validateData() }
        PlanName()
    }

    private var title = DATA.EMPTY
    private fun validateData() {
        //get data
        title = binding!!.categoryEt.text.toString().trim { it <= ' ' }

        //validate data
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(context, "Enter Title...", Toast.LENGTH_SHORT).show()
        } else if (imageUri == null) {
            Toast.makeText(context, "Pick Image...", Toast.LENGTH_SHORT).show()
        } else {
            uploadToStorage()
        }
    }

    private fun PlanName() {
        val reference = FirebaseDatabase.getInstance().getReference(DATA.PLANS).child(planId!!)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val plan = dataSnapshot.getValue(Plan::class.java)!!

                val planName = DATA.EMPTY + plan.name
                binding!!.plan.text = planName
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun uploadToStorage() {
        dialog!!.setMessage("Uploading Category...")
        dialog!!.show()
        val ref = FirebaseDatabase.getInstance().getReference(DATA.CATEGORIES)
        val id = ref.push().key
        val filePathAndName = "Images/Category/$id"
        val reference = FirebaseStorage.getInstance()
            .getReference(filePathAndName + DATA.DOT + VOID.getFileExtension(imageUri, context))
        reference.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot ->
                val uriTask = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedImageUrl = DATA.EMPTY + uriTask.result
                uploadInfoDB(uploadedImageUrl, id, ref)
            }.addOnFailureListener { e: Exception ->
                dialog!!.dismiss()
                Toast.makeText(
                    context, "Category upload failed due to " + e.message, Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun uploadInfoDB(uploadedImageUrl: String, id: String?, ref: DatabaseReference) {
        dialog!!.setMessage("Uploading category info...")
        dialog!!.show()
        val hashMap = HashMap<String?, Any?>()
        hashMap[DATA.PUBLISHER] = DATA.EMPTY + DATA.FirebaseUserUid
        hashMap[DATA.TIMESTAMP] = System.currentTimeMillis()
        hashMap[DATA.ID] = id
        hashMap[DATA.NAME] = DATA.EMPTY + title
        hashMap[DATA.PLAN] = DATA.EMPTY + planId
        hashMap[DATA.IMAGE] = uploadedImageUrl
        assert(id != null)
        ref.child(id!!).setValue(hashMap).addOnSuccessListener {
            dialog!!.dismiss()
            getItems(id)
            Toast.makeText(context, "Successfully uploaded...", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e: Exception ->
            dialog!!.dismiss()
            Toast.makeText(
                context, "Failure to upload to db due to : " + e.message, Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun getItems(categoryId: String?) {
        val ref = FirebaseDatabase.getInstance().getReference(DATA.OBJECTS)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (data in dataSnapshot.children) {
                    val `object` = data.getValue(OBJECT::class.java)!!
                    if (`object`.publisher == DATA.FirebaseUserUid) checkObject(
                        `object`.id, categoryId, `object`.name, `object`.points
                    )
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun checkObject(objectId: String?, categoryId: String?, name: String?, points: Int) {
        val reference = FirebaseDatabase.getInstance().getReference(DATA.PLANS)
            .child(planId!!).child(DATA.AUTO_TASKS)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child(objectId!!).exists()) {
                    addAutoTasks(name, points, categoryId)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun addAutoTasks(name: String?, point: Int, categoryId: String?) {
        dialog!!.setMessage("Uploading Objects...")
        val ref = FirebaseDatabase.getInstance().getReference(DATA.TASKS)
        val id = ref.push().key
        val hashMap = HashMap<String?, Any?>()
        hashMap[DATA.PUBLISHER] = DATA.EMPTY + DATA.FirebaseUserUid
        hashMap[DATA.ID] = id
        hashMap[DATA.NAME] = DATA.EMPTY + name
        hashMap[DATA.POINTS] = point
        hashMap[DATA.AVAILABLE_POINTS] = DATA.ZERO
        hashMap[DATA.RANK] = DATA.ZERO
        hashMap[DATA.CATEGORY] = DATA.EMPTY + categoryId
        hashMap[DATA.TIMESTAMP] = System.currentTimeMillis()
        hashMap[DATA.START] = DATA.ZERO
        hashMap[DATA.END] = DATA.ZERO
        assert(id != null)
        ref.child(id!!).setValue(hashMap)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == RESULT_OK) {
            val uri = CropImage.getPickImageResultUri(context, data)
            if (CropImage.isReadExternalStoragePermissionsRequired(context, uri)) {
                imageUri = uri
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 0)
            } else {
                VOID.CropImageSquare(activity)
            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                imageUri = result.uri
                binding!!.image.setImageURI(imageUri)
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                Toast.makeText(this, "Error! $error", Toast.LENGTH_SHORT).show()
            }
        }
    }
}