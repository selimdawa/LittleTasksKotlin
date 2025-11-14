package com.flatcode.littletasks.Activity

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littletasks.Model.Category
import com.flatcode.littletasks.R
import com.flatcode.littletasks.Unit.DATA
import com.flatcode.littletasks.Unit.THEME
import com.flatcode.littletasks.Unit.VOID
import com.flatcode.littletasks.databinding.ActivityTaskAddBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TaskAddActivity : AppCompatActivity() {

    private var binding: ActivityTaskAddBinding? = null
    private val context: Context = this@TaskAddActivity
    private var dialog: ProgressDialog? = null
    private var categoryId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        THEME.setThemeOfApp(context)
        super.onCreate(savedInstanceState)
        binding = ActivityTaskAddBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        categoryId = intent.getStringExtra(DATA.CATEGORY_ID)

        dialog = ProgressDialog(context)
        dialog!!.setTitle("Please wait...")
        dialog!!.setCanceledOnTouchOutside(false)

        binding!!.toolbar.nameSpace.setText(R.string.add_new_task)
        binding!!.toolbar.back.setOnClickListener { onBackPressed() }
        binding!!.toolbar.ok.setOnClickListener { validateData() }

        loadCategories()
    }

    private var name = DATA.EMPTY
    private var points = DATA.ZERO.toString() + DATA.EMPTY
    private fun validateData() {
        //get data
        name = binding!!.nameEt.text.toString().trim { it <= ' ' }
        points = binding!!.PointsEt.text.toString().trim { it <= ' ' }

        //validate data
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(context, "Enter Name...", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(points)) {
            Toast.makeText(context, "Enter Points...", Toast.LENGTH_SHORT).show()
        } else {
            uploadTaskInfoDB()
        }
    }

    private fun uploadTaskInfoDB() {
        dialog!!.setMessage("Uploading task info...")
        val ref = FirebaseDatabase.getInstance().getReference(DATA.TASKS)
        val id = ref.push().key
        val point = points.toInt()
        //setup data to upload
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
        ref.child(id!!).setValue(hashMap).addOnSuccessListener {
            dialog!!.dismiss()
            Toast.makeText(context, "Successfully uploaded...", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e: Exception ->
            dialog!!.dismiss()
            Toast.makeText(
                context, "Failure to upload to db due to :" + e.message, Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun loadCategories() {
        val ref = FirebaseDatabase.getInstance().getReference(DATA.CATEGORIES).child(categoryId!!)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val item = snapshot.getValue(Category::class.java)!!
                val categoryTitle = DATA.EMPTY + item.name
                val categoryImage = DATA.EMPTY + item.image
                binding!!.category.text = categoryTitle
                VOID.GlideImage(false, context, categoryImage, binding!!.image)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}