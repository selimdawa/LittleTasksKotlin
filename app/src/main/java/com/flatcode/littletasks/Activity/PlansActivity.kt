package com.flatcode.littletasks.Activity

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littletasks.Adapter.PlanAdapter
import com.flatcode.littletasks.Model.Plan
import com.flatcode.littletasks.R
import com.flatcode.littletasks.Unit.CLASS
import com.flatcode.littletasks.Unit.DATA
import com.flatcode.littletasks.Unit.THEME
import com.flatcode.littletasks.Unit.VOID
import com.flatcode.littletasks.databinding.ActivityPlansBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.MessageFormat

class PlansActivity : AppCompatActivity() {

    private var _binding: ActivityPlansBinding? = null
    private val binding get() = _binding!!

    private val context: Context = this@PlansActivity
    private val list = ArrayList<Plan?>()
    private var adapter: PlanAdapter? = null
    private var isNew = true

    override fun onCreate(savedInstanceState: Bundle?) {
        THEME.setThemeOfApp(context)
        super.onCreate(savedInstanceState)
        _binding = ActivityPlansBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val newPlan = intent.getStringExtra(DATA.NEW_PLAN)
        isNew = newPlan == "true"

        binding.toolbar.nameSpace.setText(R.string.plans)
        binding.toolbar.back.setOnClickListener { handleBackPressed() }
        binding.toolbar.close.setOnClickListener { handleBackPressed() }
        binding.add.add.setText(R.string.add_plan)
        binding.add.item.setOnClickListener { VOID.Intent1(context, CLASS.PLAN_ADD) }

        binding.toolbar.search.setOnClickListener {
            binding.toolbar.toolbar.visibility = View.GONE
            binding.toolbar.toolbarSearch.visibility = View.VISIBLE
            DATA.searchStatus = true
        }

        binding.toolbar.textSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter?.filter?.filter(s)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        adapter = PlanAdapter(context, list, isNew)
        binding.recyclerView.adapter = adapter
    }

    private fun loadItems() {
        val uid = DATA.FirebaseUserUid
        FirebaseDatabase.getInstance().getReference(DATA.PLANS)
            .orderByChild("publisher")
            .equalTo(uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val newList = ArrayList<Plan?>()
                    for (data in dataSnapshot.children) {
                        val item = data.getValue(Plan::class.java) ?: continue
                        newList.add(item)
                    }

                    val oldSize = list.size
                    val newSize = newList.size

                    if (list != newList) {
                        list.clear()
                        list.addAll(newList)

                        adapter?.let { adp ->
                            adp.notifyItemRangeRemoved(0, oldSize)
                            adp.notifyItemRangeInserted(0, newSize)
                        }
                    }

                    _binding?.let { b ->
                        b.toolbar.number.text = MessageFormat.format("( {0} )", list.size)
                        b.bar.visibility = View.GONE
                        if (list.isNotEmpty()) {
                            b.recyclerView.visibility = View.VISIBLE
                            b.emptyText.visibility = View.GONE
                        } else {
                            b.recyclerView.visibility = View.GONE
                            b.emptyText.visibility = View.VISIBLE
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    private fun handleBackPressed() {
        if (DATA.searchStatus) {
            binding.toolbar.toolbar.visibility = View.VISIBLE
            binding.toolbar.toolbarSearch.visibility = View.GONE
            DATA.searchStatus = false
            binding.toolbar.textSearch.setText(DATA.EMPTY)
        } else {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        loadItems()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}