package com.flatcode.littletasks.Activity

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littletasks.Model.OBJECT
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

    private var binding: ActivityObjectEditBinding? = null
    var context: Context = this@ObjectEditActivity
    var id: String? = null
    private var dialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        THEME.setThemeOfApp(context)
        super.onCreate(savedInstanceState)
        binding = ActivityObjectEditBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        id = intent.getStringExtra(DATA.ID)

        dialog = ProgressDialog(context)
        dialog!!.setTitle("Please wait")
        dialog!!.setCanceledOnTouchOutside(false)

        loadInfo()
        binding!!.toolbar.nameSpace.setText(R.string.edit_object)
        binding!!.toolbar.back.setOnClickListener { onBackPressed() }
        binding!!.name.setText(R.string.object_name)
        binding!!.go.setOnClickListener { validateData() }
    }

    private var name = DATA.EMPTY
    private var points = DATA.EMPTY
    private fun validateData() {
        name = binding!!.nameEt.text.toString().trim { it <= ' ' }
        points = binding!!.PointsEt.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(context, "Enter name...", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(points)) {
            Toast.makeText(context, "Enter points...", Toast.LENGTH_SHORT).show()
        } else {
            Update()
        }
    }

    private fun Update() {
        dialog!!.setMessage("Updating Object...")
        dialog!!.show()
        val point = points.toInt()
        val hashMap = HashMap<String?, Any>()
        hashMap[DATA.NAME] = DATA.EMPTY + name
        hashMap[DATA.POINTS] = point
        val reference = FirebaseDatabase.getInstance().getReference(DATA.OBJECTS)
        reference.child(id!!).updateChildren(hashMap).addOnSuccessListener {
            dialog!!.dismiss()
            Toast.makeText(context, "Object updated...", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e: Exception ->
            dialog!!.dismiss()
            Toast.makeText(context, "Failed to update db duo to " + e.message, Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun loadInfo() {
        val reference = FirebaseDatabase.getInstance().getReference(DATA.OBJECTS)
        reference.child(id!!).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val item = snapshot.getValue(OBJECT::class.java)!!
                val name = item.name
                val points = item.points

                binding!!.nameEt.setText(name)
                binding!!.PointsEt.setText(MessageFormat.format("{0}{1}", DATA.EMPTY, points))
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}