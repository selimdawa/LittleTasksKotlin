package com.flatcode.littletasks.Activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.flatcode.littletasks.R
import com.flatcode.littletasks.Unit.CLASS
import com.flatcode.littletasks.Unit.DATA
import com.flatcode.littletasks.Unit.VOID
import com.flatcode.littletasks.databinding.ActivityMainBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.selimdawa.bubblebottom.BubbleBottomNavigation

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private var activity: Activity? = null
    private val context: Context = also { activity = it }

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                VOID.closeApp(this@MainActivity)
            }
        })

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainer) as NavHostFragment
        navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->
            _binding?.toolbar?.card?.visibility = if (destination.id == R.id.homeFragment) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        binding.bottomNavigation.apply {
            setupAppMenu()
            setOnShowListener { item -> navController.navigate(item.id) }
            show(R.id.homeFragment, true)
        }

        binding.toolbar.image.setOnClickListener {
            VOID.IntentExtra(context, CLASS.PROFILE, DATA.PROFILE_ID, DATA.FirebaseUserUid)
        }

        loadUserInfo()
    }

    private fun loadUserInfo() {
        val uid = DATA.FirebaseUserUid
        FirebaseDatabase.getInstance().getReference(DATA.USERS).child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val profileImage = DATA.EMPTY + snapshot.child(DATA.PROFILE_IMAGE).value
                    _binding?.let { b ->
                        VOID.GlideImage(true, this@MainActivity, profileImage, b.toolbar.image)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        activity = null
        _binding = null
    }

    fun BubbleBottomNavigation.setupAppMenu() {
        add(BubbleBottomNavigation.Model(R.id.settingsFragment, R.drawable.ic_settings))
        add(BubbleBottomNavigation.Model(R.id.homeFragment, R.drawable.ic_home))
        add(BubbleBottomNavigation.Model(R.id.categoriesFragment, R.drawable.ic_group))
        show(R.id.homeFragment, true)
    }

    companion object {
    }
}