package com.flatcode.littletasks.Activity

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littletasks.Model.Task
import com.flatcode.littletasks.Model.User
import com.flatcode.littletasks.Unit.CLASS
import com.flatcode.littletasks.Unit.DATA
import com.flatcode.littletasks.Unit.THEME
import com.flatcode.littletasks.Unit.VOID
import com.flatcode.littletasks.databinding.ActivityProfileBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.MessageFormat

class ProfileActivity : AppCompatActivity() {

    private var binding: ActivityProfileBinding? = null
    var context: Context = this@ProfileActivity
    var profileId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        THEME.setThemeOfApp(context)
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        val intent = intent
        profileId = intent.getStringExtra(DATA.PROFILE_ID)

        binding!!.edit.setOnClickListener { VOID.Intent1(context, CLASS.PROFILE_EDIT) }
        binding!!.back.setOnClickListener { onBackPressed() }
    }

    private fun init() {
        loadUserInfo()
        getNrItems(DATA.TASKS, binding!!.numberTasks)
        getNrItems(DATA.PLANS, binding!!.numberPlans)
        getNrItems(DATA.OBJECTS, binding!!.numberObjects)
        getNrItems(DATA.CATEGORIES, binding!!.numberCategories)
    }

    private fun loadUserInfo() {
        val reference = FirebaseDatabase.getInstance().getReference(DATA.USERS)
        reference.child(profileId!!).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val item = snapshot.getValue(User::class.java)!!
                val username = DATA.EMPTY + item.username
                val profileImage = DATA.EMPTY + item.profileImage

                binding!!.username.text = username
                VOID.GlideImage(true, context, profileImage, binding!!.profile)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun getNrItems(database: String?, text: TextView) {
        val reference = FirebaseDatabase.getInstance().getReference(database!!)
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var i = 0
                for (data in dataSnapshot.children) {
                    val item = data.getValue(Task::class.java)!!
                    if (item.publisher == profileId) i++
                }
                text.text = MessageFormat.format("{0}{1}", DATA.EMPTY, i)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    override fun onRestart() {
        super.onRestart()
        init()
    }

    override fun onResume() {
        super.onResume()
        init()
    }
}