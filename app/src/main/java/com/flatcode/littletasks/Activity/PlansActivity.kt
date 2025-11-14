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
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import java.text.MessageFormat

class PlansActivity : AppCompatActivity() {

    private var binding: ActivityPlansBinding? = null
    var context: Context = this@PlansActivity
    var list: ArrayList<Plan?>? = null
    var adapter: PlanAdapter? = null
    var newPlan: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        THEME.setThemeOfApp(context)
        super.onCreate(savedInstanceState)
        binding = ActivityPlansBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        val intent = intent
        newPlan = intent.getStringExtra(DATA.NEW_PLAN)

        binding!!.toolbar.nameSpace.setText(R.string.plans)
        binding!!.toolbar.back.setOnClickListener { onBackPressed() }
        binding!!.toolbar.close.setOnClickListener { onBackPressed() }
        binding!!.add.add.setText(R.string.add_plan)
        binding!!.add.item.setOnClickListener { VOID.Intent1(context, CLASS.PLAN_ADD) }

        if (newPlan == "true") isNew = true else if (newPlan == "false") isNew = false
        binding!!.toolbar.search.setOnClickListener {
            binding!!.toolbar.toolbar.visibility = View.GONE
            binding!!.toolbar.toolbarSearch.visibility = View.VISIBLE
            DATA.searchStatus = true
        }
        binding!!.toolbar.textSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                try {
                    adapter!!.filter.filter(s)
                } catch (e: Exception) {
                    //None
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })

        //binding.recyclerView.setHasFixedSize(true);
        list = ArrayList()
        adapter = PlanAdapter(context, list!!, isNew)
        binding!!.recyclerView.adapter = adapter
    }

    private val items: Unit
        get() {
            val ref: Query = FirebaseDatabase.getInstance().getReference(DATA.PLANS)
            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    list!!.clear()
                    var i = 0
                    for (data in dataSnapshot.children) {
                        val item = data.getValue(Plan::class.java)!!
                        if (item.publisher == DATA.FirebaseUserUid) {
                            list!!.add(item)
                            i++
                        }
                    }
                    binding!!.toolbar.number.text = MessageFormat.format("( {0} )", i)
                    binding!!.bar.visibility = View.GONE
                    if (list!!.isNotEmpty()) {
                        binding!!.recyclerView.visibility = View.VISIBLE
                        binding!!.emptyText.visibility = View.GONE
                    } else {
                        binding!!.recyclerView.visibility = View.GONE
                        binding!!.emptyText.visibility = View.VISIBLE
                    }
                    adapter!!.notifyDataSetChanged()
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }

    override fun onBackPressed() {
        if (DATA.searchStatus) {
            binding!!.toolbar.toolbar.visibility = View.VISIBLE
            binding!!.toolbar.toolbarSearch.visibility = View.GONE
            DATA.searchStatus = false
            binding!!.toolbar.textSearch.setText(DATA.EMPTY)
        } else super.onBackPressed()
    }

    override fun onResume() {
        super.onResume()
        items
    }

    override fun onRestart() {
        super.onRestart()
        items
    }

    companion object {
        private var isNew = true
    }
}