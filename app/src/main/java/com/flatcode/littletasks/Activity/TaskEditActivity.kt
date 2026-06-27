package com.flatcode.littletasks.Activity

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littletasks.Model.Category
import com.flatcode.littletasks.Model.Task
import com.flatcode.littletasks.R
import com.flatcode.littletasks.Unit.DATA
import com.flatcode.littletasks.Unit.THEME
import com.flatcode.littletasks.Unit.VOID
import com.flatcode.littletasks.databinding.ActivityTaskAddBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TaskEditActivity : AppCompatActivity() {

    private var _binding: ActivityTaskAddBinding? = null
    private val binding get() = _binding!!

    private val context: Context = this@TaskEditActivity
    private var progressDialog: AlertDialog? = null
    private var taskId: String? = null
    private var categoryId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        THEME.setThemeOfApp(context)
        super.onCreate(savedInstanceState)
        _binding = ActivityTaskAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        taskId = intent.getStringExtra(DATA.TASK_ID)
        categoryId = intent.getStringExtra(DATA.CATEGORY_ID)

        loadBooksInfo()
        loadCategories()

        binding.toolbar.nameSpace.setText(R.string.edit_task)
        binding.toolbar.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.name.setText(R.string.task_name)
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
        val tId = taskId ?: return
        showLoading()

        val point = points.toIntOrNull() ?: 0
        val hashMap = HashMap<String, Any>().apply {
            put(DATA.NAME, name)
            put(DATA.POINTS, point)
        }

        FirebaseDatabase.getInstance().getReference(DATA.TASKS)
            .child(tId)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                dismissLoading()
                Toast.makeText(context, "Task info updated...", Toast.LENGTH_SHORT).show()
                finish()
            }.addOnFailureListener { e ->
                dismissLoading()
                Toast.makeText(context, DATA.EMPTY + e.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadBooksInfo() {
        val tId = taskId ?: return
        FirebaseDatabase.getInstance().getReference(DATA.TASKS).child(tId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val item = snapshot.getValue(Task::class.java) ?: return
                    val points = DATA.EMPTY + item.points
                    val title = DATA.EMPTY + item.name

                    _binding?.let { b ->
                        b.nameEt.setText(title)
                        b.PointsEt.setText(points)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun loadCategories() {
        val catId = categoryId ?: return
        FirebaseDatabase.getInstance().getReference(DATA.CATEGORIES).child(catId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val item = snapshot.getValue(Category::class.java) ?: return
                    val categoryImage = DATA.EMPTY + item.image
                    val categoryName = DATA.EMPTY + item.name

                    _binding?.let { b ->
                        VOID.GlideImage(false, context, categoryImage, b.image)
                        b.category.text = categoryName
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