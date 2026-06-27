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

    private var _binding: ActivityProfileBinding? = null
    private val binding get() = _binding!!

    private val context: Context = this@ProfileActivity
    private var profileId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        THEME.setThemeOfApp(context)
        super.onCreate(savedInstanceState)
        _binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        profileId = intent.getStringExtra(DATA.PROFILE_ID)

        binding.edit.setOnClickListener { VOID.Intent1(context, CLASS.PROFILE_EDIT) }
        binding.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun init() {
        val id = profileId ?: return
        loadUserInfo(id)
        getNrItems(DATA.TASKS, id, binding.numberTasks)
        getNrItems(DATA.PLANS, id, binding.numberPlans)
        getNrItems(DATA.OBJECTS, id, binding.numberObjects)
        getNrItems(DATA.CATEGORIES, id, binding.numberCategories)
    }

    private fun loadUserInfo(id: String) {
        FirebaseDatabase.getInstance().getReference(DATA.USERS).child(id)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val item = snapshot.getValue(User::class.java) ?: return
                    val username = DATA.EMPTY + item.username
                    val profileImage = DATA.EMPTY + item.profileImage

                    _binding?.let { b ->
                        b.username.text = username
                        VOID.GlideImage(true, context, profileImage, b.profile)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun getNrItems(database: String, id: String, text: TextView) {
        FirebaseDatabase.getInstance().getReference(database)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var i = 0
                    for (data in dataSnapshot.children) {
                        val item = data.getValue(Task::class.java) ?: continue
                        if (item.publisher == id) i++
                    }
                    text.text = MessageFormat.format("{0}{1}", DATA.EMPTY, i)
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    override fun onResume() {
        super.onResume()
        init()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}