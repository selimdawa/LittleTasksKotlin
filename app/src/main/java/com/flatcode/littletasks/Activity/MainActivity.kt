package com.flatcode.littletasks.Activity

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.flatcode.littletasks.R
import com.flatcode.littletasks.Unit.CLASS
import com.flatcode.littletasks.Unit.DATA
import com.flatcode.littletasks.Unit.THEME
import com.flatcode.littletasks.Unit.VOID
import com.flatcode.littletasks.databinding.ActivityMainBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.selimdawa.bubblebottom.BubbleBottomNavigation

class MainActivity : AppCompatActivity(), OnSharedPreferenceChangeListener {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private var activity: Activity? = null
    private val context: Context = also { activity = it }

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        PreferenceManager.getDefaultSharedPreferences(baseContext)
            .registerOnSharedPreferenceChangeListener(this)
        THEME.setThemeOfApp(context)
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
            if (destination.id == R.id.homeFragment) {
                binding.toolbar.card.visibility = View.VISIBLE
            } else {
                binding.toolbar.card.visibility = View.GONE
            }
        }

        supportFragmentManager.beginTransaction().replace(R.id.settings, SettingFragment()).commit()

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
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val profileImage = DATA.EMPTY + snapshot.child(DATA.PROFILE_IMAGE).value
                    _binding?.let { b ->
                        VOID.GlideImage(true, context, profileImage, b.toolbar.image)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == DATA.COLOR_OPTION) {
            recreate()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        PreferenceManager.getDefaultSharedPreferences(baseContext)
            .unregisterOnSharedPreferenceChangeListener(this)
        activity = null
        _binding = null
    }

    class SettingFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }

    fun BubbleBottomNavigation.setupAppMenu() {
        add(BubbleBottomNavigation.Model(R.id.settingsFragment, R.drawable.ic_settings))
        add(BubbleBottomNavigation.Model(R.id.homeFragment, R.drawable.ic_home))
        add(BubbleBottomNavigation.Model(R.id.categoriesFragment, R.drawable.ic_group))
        show(R.id.homeFragment, true)
    }
}