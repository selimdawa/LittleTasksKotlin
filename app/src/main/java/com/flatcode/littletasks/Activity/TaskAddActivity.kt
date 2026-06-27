package com.flatcode.littletasks.Activity

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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

    private var _binding: ActivityTaskAddBinding? = null
    private val binding get() = _binding!!

    private val context: Context = this@TaskAddActivity
    private var progressDialog: AlertDialog? = null
    private var categoryId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        THEME.setThemeOfApp(context)
        super.onCreate(savedInstanceState)
        _binding = ActivityTaskAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        categoryId = intent.getStringExtra(DATA.CATEGORY_ID)

        binding.toolbar.nameSpace.setText(R.string.add_new_task)
        binding.toolbar.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.toolbar.ok.setOnClickListener { validateData() }

        loadCategories()
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
        val name = binding.nameEt.text.toString().trim()
        val points = binding.PointsEt.text.toString().trim()

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(context, "Enter Name...", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(points)) {
            Toast.makeText(context, "Enter Points...", Toast.LENGTH_SHORT).show()
        } else {
            uploadTaskInfoDB(name, points)
        }
    }

    private fun uploadTaskInfoDB(name: String, points: String) {
        showLoading()
        val ref = FirebaseDatabase.getInstance().getReference(DATA.TASKS)
        val id = ref.push().key ?: return
        val point = points.toIntOrNull() ?: 0

        val hashMap = HashMap<String, Any?>().apply {
            put(DATA.PUBLISHER, DATA.EMPTY + DATA.FirebaseUserUid)
            put(DATA.ID, id)
            put(DATA.NAME, DATA.EMPTY + name)
            put(DATA.POINTS, point)
            put(DATA.AVAILABLE_POINTS, DATA.ZERO)
            put(DATA.RANK, DATA.ZERO)
            put(DATA.CATEGORY, DATA.EMPTY + categoryId)
            put(DATA.TIMESTAMP, System.currentTimeMillis())
            put(DATA.START, DATA.ZERO)
            put(DATA.END, DATA.ZERO)
        }

        ref.child(id).setValue(hashMap).addOnSuccessListener {
            dismissLoading()
            Toast.makeText(context, "Successfully uploaded...", Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener { e ->
            dismissLoading()
            Toast.makeText(context, "Failure to upload to db due to :${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadCategories() {
        val catId = categoryId ?: return
        FirebaseDatabase.getInstance().getReference(DATA.CATEGORIES).child(catId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val item = snapshot.getValue(Category::class.java) ?: return
                    val categoryTitle = DATA.EMPTY + item.name
                    val categoryImage = DATA.EMPTY + item.image

                    _binding?.let { b ->
                        b.category.text = categoryTitle
                        VOID.GlideImage(false, context, categoryImage, b.image)
                    }
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