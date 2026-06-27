package com.flatcode.littletasks.Activity

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littletasks.Model.TaskItem
import com.flatcode.littletasks.R
import com.flatcode.littletasks.Unit.DATA
import com.flatcode.littletasks.Unit.THEME
import com.flatcode.littletasks.databinding.ActivityObjectEditBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.MessageFormat

class ObjectEditActivity : AppCompatActivity() {

    private var _binding: ActivityObjectEditBinding? = null
    private val binding get() = _binding!!

    private val context: Context = this@ObjectEditActivity
    private var id: String? = null
    private var progressDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        THEME.setThemeOfApp(context)
        super.onCreate(savedInstanceState)
        _binding = ActivityObjectEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        id = intent.getStringExtra(DATA.ID)

        loadInfo()
        binding.toolbar.nameSpace.setText(R.string.edit_object)
        binding.toolbar.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.name.setText(R.string.object_name)
        binding.go.setOnClickListener { validateData() }
    }

    private fun showLoading() {
        if (progressDialog == null) {
            val builder = AlertDialog.Builder(context)
            builder.setView(R.layout.layout_loading_dialog)
            builder.setCancelable(false)
            progressDialog = builder.create()
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
            Toast.makeText(context, "Enter name...", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(points)) {
            Toast.makeText(context, "Enter points...", Toast.LENGTH_SHORT).show()
        } else {
            updateObject(name, points)
        }
    }

    private fun updateObject(name: String, points: String) {
        val objectId = id ?: return
        showLoading()

        val point = points.toIntOrNull() ?: 0
        val hashMap = HashMap<String, Any>().apply {
            put(DATA.NAME, name)
            put(DATA.POINTS, point)
        }

        FirebaseDatabase.getInstance().getReference(DATA.OBJECTS)
            .child(objectId)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                dismissLoading()
                Toast.makeText(context, "Object updated...", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e ->
                dismissLoading()
                Toast.makeText(
                    context,
                    "Failed to update db due to: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun loadInfo() {
        val objectId = id ?: return
        FirebaseDatabase.getInstance().getReference(DATA.OBJECTS).child(objectId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val item = snapshot.getValue(TaskItem::class.java) ?: return

                    _binding?.let { b ->
                        b.nameEt.setText(item.name)
                        b.PointsEt.setText(MessageFormat.format("{0}{1}", DATA.EMPTY, item.points))
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