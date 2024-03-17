package com.flatcode.littletasks.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.flatcode.littletasks.Adapter.SettingAdapter
import com.flatcode.littletasks.Model.Category
import com.flatcode.littletasks.Model.OBJECT
import com.flatcode.littletasks.Model.Plan
import com.flatcode.littletasks.Model.Setting
import com.flatcode.littletasks.Model.Task
import com.flatcode.littletasks.Model.User
import com.flatcode.littletasks.R
import com.flatcode.littletasks.Unit.CLASS
import com.flatcode.littletasks.Unit.DATA
import com.flatcode.littletasks.Unit.VOID
import com.flatcode.littletasks.databinding.FragmentSettingsBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.MessageFormat
import java.util.Objects

class SettingsFragment : Fragment() {

    private var binding: FragmentSettingsBinding? = null
    private var list: ArrayList<Setting>? = null
    private var adapter: SettingAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSettingsBinding.inflate(LayoutInflater.from(context), container, false)

        //binding!!.recyclerView.setHasFixedSize(true)
        list = ArrayList()
        adapter = SettingAdapter(context, list!!)
        binding!!.recyclerView.adapter = adapter

        binding!!.toolbar.item.setOnClickListener {
            VOID.IntentExtra(context, CLASS.PROFILE, DATA.PROFILE_ID, DATA.FirebaseUserUid)
        }
        return binding!!.root
    }

    var C = 0
    var P = 0
    var O = 0
    var F = 0
    private fun nrItems() {
        val reference = FirebaseDatabase.getInstance().getReference(DATA.CATEGORIES)
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                C = 0
                for (data in dataSnapshot.children) {
                    val item = data.getValue(Category::class.java)!!
                    if (item.id != null) if (item.publisher == DATA.FirebaseUserUid) C++
                }
                nrPlans()
            }

            private fun nrPlans() {
                val reference = FirebaseDatabase.getInstance().getReference(DATA.PLANS)
                reference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        P = 0
                        for (data in dataSnapshot.children) {
                            val item = data.getValue(Plan::class.java)!!
                            if (item.id != null) if (item.publisher == DATA.FirebaseUserUid) P++
                        }
                        nrObjects()
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
            }

            private fun nrObjects() {
                val reference = FirebaseDatabase.getInstance().getReference(DATA.OBJECTS)
                reference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        O = 0
                        for (data in dataSnapshot.children) {
                            val item = data.getValue(OBJECT::class.java)!!
                            if (item.id != null) if (item.publisher == DATA.FirebaseUserUid) O++
                        }
                        nrFavorites()
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
            }

            private fun nrFavorites() {
                val reference = FirebaseDatabase.getInstance().getReference(DATA.FAVORITES)
                    .child(DATA.FirebaseUserUid)
                reference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        F = O
                        F = dataSnapshot.childrenCount.toInt()
                        loadSettings(C, P, O, F)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun loadUserInfo() {
        val reference = FirebaseDatabase.getInstance().getReference(DATA.USERS)
        reference.child(Objects.requireNonNull(DATA.FirebaseUserUid))
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val item = snapshot.getValue(User::class.java)!!
                    val ProfileImage = item.profileImage
                    val Username = item.username

                    VOID.GlideImage(true, context, ProfileImage, binding!!.toolbar.imageProfile)
                    binding!!.toolbar.username.text = Username
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun loadSettings(categories: Int, plans: Int, objects: Int, favorites: Int) {
        list!!.clear()
        val item = Setting("1", "Edit Profile", R.drawable.ic_edit_white, 0, CLASS.PROFILE_EDIT)
        val item2 =
            Setting("2", "Categories", R.drawable.ic_category, categories, CLASS.CATEGORIES)
        val item3 = Setting("4", "Plans", R.drawable.ic_list, plans, DATA.PLANS)
        val item4 = Setting("7", "Objects", R.drawable.ic_object, objects, CLASS.OBJECTS)
        val item5 =
            Setting("9", "Favorites", R.drawable.ic_star_selected, favorites, CLASS.FAVORITES)
        val item6 = Setting("10", "About App", R.drawable.ic_info, 0)
        val item7 = Setting("11", "Logout", R.drawable.ic_logout_white, 0)
        val item8 = Setting("12", "Share App", R.drawable.ic_share, 0)
        val item9 = Setting("13", "Rate APP", R.drawable.ic_heart_selected, 0)
        val item10 =
            Setting("14", "Privacy Policy", R.drawable.ic_privacy_policy, 0, CLASS.PRIVACY_POLICY)
        list!!.add(item)
        list!!.add(item2)
        list!!.add(item3)
        list!!.add(item4)
        list!!.add(item5)
        list!!.add(item6)
        list!!.add(item7)
        list!!.add(item8)
        list!!.add(item9)
        list!!.add(item10)
        adapter!!.notifyDataSetChanged()
    }

    private val points: Unit
        private get() {
            val ref = FirebaseDatabase.getInstance().getReference(DATA.TASKS)
            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var i = 0
                    var a = 0
                    for (snapshot in dataSnapshot.children) {
                        val item = snapshot.getValue(Task::class.java)!!
                        i = i + item.points
                        a = a + item.aVPoints
                    }
                    binding!!.toolbar.all.text = MessageFormat.format("{0}{1}", DATA.EMPTY, i)
                    binding!!.toolbar.availablePoints.text =
                        MessageFormat.format("{0}{1}", DATA.EMPTY, a)
                    binding!!.toolbar.level.text =
                        MessageFormat.format("{0}{1}", DATA.EMPTY, VOID.levelPoint(a, 10))
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }

    override fun onResume() {
        loadUserInfo()
        points
        nrItems()
        super.onResume()
    }
}