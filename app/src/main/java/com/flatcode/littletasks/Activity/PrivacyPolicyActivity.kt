package com.flatcode.littletasks.Activity

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littletasks.R
import com.flatcode.littletasks.Unit.DATA
import com.flatcode.littletasks.Unit.THEME
import com.flatcode.littletasks.databinding.ActivityPrivacyPolicyBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PrivacyPolicyActivity : AppCompatActivity() {

    private var _binding: ActivityPrivacyPolicyBinding? = null
    private val binding get() = _binding!!

    private val context: Context = this@PrivacyPolicyActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        THEME.setThemeOfApp(context)
        super.onCreate(savedInstanceState)
        _binding = ActivityPrivacyPolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.nameSpace.setText(R.string.privacy_policy)
        binding.toolbar.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.toolbar.search.visibility = View.GONE
    }

    private fun privacyPolicy() {
        FirebaseDatabase.getInstance().getReference(DATA.TOOLS).child(DATA.PRIVACY_POLICY)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val name = dataSnapshot.value?.toString().orEmpty()
                    _binding?.text?.text = name
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    override fun onResume() {
        super.onResume()
        privacyPolicy()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}