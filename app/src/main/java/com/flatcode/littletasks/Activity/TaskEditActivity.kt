package com.flatcode.littletasks.Activity

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
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

    private var binding: ActivityTaskAddBinding? = null
    private val context: Context = this@TaskEditActivity
    private var dialog: ProgressDialog? = null
    private var taskId: String? = null
    private var categoryId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        THEME.setThemeOfApp(context)
        super.onCreate(savedInstanceState)
        binding = ActivityTaskAddBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        taskId = intent.getStringExtra(DATA.TASK_ID)
        categoryId = intent.getStringExtra(DATA.CATEGORY_ID)

        dialog = ProgressDialog(context)
        dialog!!.setTitle("Please wait...")
        dialog!!.setCanceledOnTouchOutside(false)
        loadBooksInfo()
        loadCategories()

        binding!!.toolbar.nameSpace.setText(R.string.edit_task)
        binding!!.toolbar.back.setOnClickListener { onBackPressed() }
        binding!!.name.setText(R.string.task_name)
        binding!!.toolbar.ok.setOnClickListener { validateData() }
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
        dialog!!.show()
        val point = points.toInt()
        val hashMap = HashMap<String?, Any>()
        hashMap[DATA.NAME] = DATA.EMPTY + name
        hashMap[DATA.POINTS] = point
        val ref = FirebaseDatabase.getInstance().getReference(DATA.TASKS)
        ref.child(taskId!!).updateChildren(hashMap).addOnSuccessListener {
            dialog!!.dismiss()
            Toast.makeText(context, "Task info updated...", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e: Exception ->
            dialog!!.dismiss()
            Toast.makeText(context, DATA.EMPTY + e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadBooksInfo() {
        val refBooks = FirebaseDatabase.getInstance().getReference(DATA.TASKS)
        refBooks.child(taskId!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val item = snapshot.getValue(Task::class.java)!!
                val points = DATA.EMPTY + item.points
                val title = DATA.EMPTY + item.name

                binding!!.nameEt.setText(title)
                binding!!.PointsEt.setText(points)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun loadCategories() {
        val ref = FirebaseDatabase.getInstance().getReference(DATA.CATEGORIES).child(
            categoryId!!
        )
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val item = snapshot.getValue(Category::class.java)!!
                val categoryImage = DATA.EMPTY + item.image
                val categoryName = DATA.EMPTY + item.name

                VOID.GlideImage(false, context, categoryImage, binding!!.image)
                binding!!.category.text = categoryName
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}