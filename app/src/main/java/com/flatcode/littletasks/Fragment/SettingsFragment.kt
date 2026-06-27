package com.flatcode.littletasks.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.flatcode.littletasks.Adapter.SettingAdapter
import com.flatcode.littletasks.Model.Category
import com.flatcode.littletasks.Model.Plan
import com.flatcode.littletasks.Model.Setting
import com.flatcode.littletasks.Model.Task
import com.flatcode.littletasks.Model.TaskItem
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

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val list = ArrayList<Setting>()
    private var adapter: SettingAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        initStaticSettings()
        adapter = SettingAdapter(context, list)
        binding.recyclerView.adapter = adapter

        binding.toolbar.item.setOnClickListener {
            VOID.IntentExtra(context, CLASS.PROFILE, DATA.PROFILE_ID, DATA.FirebaseUserUid)
        }
        return binding.root
    }

    private fun initStaticSettings() {
        list.clear()
        list.add(Setting(id = "1", name = "Edit Profile", image = R.drawable.ic_edit_white, number = 0, c = CLASS.PROFILE_EDIT))
        list.add(Setting(id = "2", name = "Categories", image = R.drawable.ic_category, number = 0, c = CLASS.CATEGORIES))
        list.add(Setting(id = "4", name = "Plans", image = R.drawable.ic_list, number = 0, type = DATA.PLANS))
        list.add(Setting(id = "7", name = "Objects", image = R.drawable.ic_object, number = 0, c = CLASS.OBJECTS))
        list.add(Setting(id = "9", name = "Favorites", image = R.drawable.ic_star_selected, number = 0, c = CLASS.FAVORITES))
        list.add(Setting(id = "10", name = "About App", image = R.drawable.ic_info, number = 0))
        list.add(Setting(id = "11", name = "Logout", image = R.drawable.ic_logout_white, number = 0))
        list.add(Setting(id = "12", name = "Share App", image = R.drawable.ic_share, number = 0))
        list.add(Setting(id = "13", name = "Rate APP", image = R.drawable.ic_heart_selected, number = 0))
        list.add(Setting(id = "14", name = "Privacy Policy", image = R.drawable.ic_privacy_policy, number = 0, c = CLASS.PRIVACY_POLICY))
    }

    private fun updateSettingNumber(index: Int, count: Int) {
        if (index in list.indices && list[index].number != count) {
            list[index].number = count
            adapter?.notifyItemChanged(index)
        }
    }

    private fun nrItems() {
        val uid = DATA.FirebaseUserUid
        val database = FirebaseDatabase.getInstance()

        database.getReference(DATA.CATEGORIES).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val c = snapshot.children.mapNotNull { it.getValue(Category::class.java) }
                    .count { it.id != null && it.publisher == uid }
                updateSettingNumber(1, c)

                database.getReference(DATA.PLANS).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val p = snapshot.children.mapNotNull { it.getValue(Plan::class.java) }
                            .count { it.id != null && it.publisher == uid }
                        updateSettingNumber(2, p)

                        database.getReference(DATA.OBJECTS).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val o = snapshot.children.mapNotNull { it.getValue(TaskItem::class.java) }
                                    .count { it.id != null && it.publisher == uid }
                                updateSettingNumber(3, o)

                                database.getReference(DATA.FAVORITES).child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val f = snapshot.childrenCount.toInt()
                                        updateSettingNumber(4, f)
                                    }
                                    override fun onCancelled(error: DatabaseError) {}
                                })
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun loadUserInfo() {
        val uid = DATA.FirebaseUserUid
        FirebaseDatabase.getInstance().getReference(DATA.USERS).child(uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val item = snapshot.getValue(User::class.java) ?: return
                    VOID.GlideImage(true, context, item.profileImage, binding.toolbar.imageProfile)
                    binding.toolbar.username.text = item.username
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun loadPoints() {
        FirebaseDatabase.getInstance().getReference(DATA.TASKS)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var totalPoints = 0
                    var availablePoints = 0

                    for (snapshot in dataSnapshot.children) {
                        val item = snapshot.getValue(Task::class.java) ?: continue
                        totalPoints += item.points
                        availablePoints += item.aVPoints
                    }

                    _binding?.let { b ->
                        b.toolbar.all.text = MessageFormat.format("{0}{1}", DATA.EMPTY, totalPoints)
                        b.toolbar.availablePoints.text = MessageFormat.format("{0}{1}", DATA.EMPTY, availablePoints)
                        b.toolbar.level.text = MessageFormat.format("{0}{1}", DATA.EMPTY, VOID.levelPoint(availablePoints, 10))
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    override fun onResume() {
        super.onResume()
        loadUserInfo()
        loadPoints()
        nrItems()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}