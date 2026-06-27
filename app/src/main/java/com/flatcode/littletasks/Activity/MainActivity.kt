package com.flatcode.littletasks.Activity

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.flatcode.littletasks.Fragment.CategoriesFragment
import com.flatcode.littletasks.Fragment.HomeFragment
import com.flatcode.littletasks.Fragment.SettingsFragment
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
import com.nafis.bottomnavigation.NafisBottomNavigation

class MainActivity : AppCompatActivity(), OnSharedPreferenceChangeListener {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private var activity: Activity? = null
    private val context: Context = also { activity = it }

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

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingFragment())
            .commit()

        binding.bottomNavigation.apply {
            add(NafisBottomNavigation.Model(1, R.drawable.ic_settings))
            add(NafisBottomNavigation.Model(2, R.drawable.ic_home))
            add(NafisBottomNavigation.Model(3, R.drawable.ic_group))

            setOnShowListener { item ->
                val fragment = when (item.id) {
                    1 -> {
                        _binding?.toolbar?.card?.visibility = View.GONE
                        SettingsFragment()
                    }
                    2 -> {
                        _binding?.toolbar?.card?.visibility = View.VISIBLE
                        HomeFragment()
                    }
                    3 -> {
                        _binding?.toolbar?.card?.visibility = View.GONE
                        CategoriesFragment()
                    }
                    else -> null
                }
                loadFragment(fragment)
            }

            setOnClickMenuListener { item ->
                val textRes = when (item.id) {
                    1 -> R.string.settings
                    2 -> R.string.home
                    3 -> R.string.categories
                    else -> return@setOnClickMenuListener
                }
                Toast.makeText(applicationContext, textRes, Toast.LENGTH_SHORT).show()
            }

            setOnReselectListener { item ->
                val textRes = when (item.id) {
                    1 -> R.string.settings
                    2 -> R.string.home
                    3 -> R.string.categories
                    else -> return@setOnReselectListener
                }
                Toast.makeText(applicationContext, textRes, Toast.LENGTH_SHORT).show()
            }

            show(2, true)
        }

        binding.toolbar.image.setOnClickListener {
            VOID.IntentExtra(context, CLASS.PROFILE, DATA.PROFILE_ID, DATA.FirebaseUserUid)
        }

        loadUserInfo()
    }

    private fun loadFragment(fragment: Fragment?) {
        fragment?.let {
            supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, it).commit()
        }
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
}