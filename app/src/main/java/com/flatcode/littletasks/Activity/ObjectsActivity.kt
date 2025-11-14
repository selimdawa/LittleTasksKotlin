package com.flatcode.littletasks.Activity

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littletasks.Adapter.ObjectAdapter
import com.flatcode.littletasks.Model.OBJECT
import com.flatcode.littletasks.R
import com.flatcode.littletasks.Unit.CLASS
import com.flatcode.littletasks.Unit.DATA
import com.flatcode.littletasks.Unit.THEME
import com.flatcode.littletasks.Unit.VOID
import com.flatcode.littletasks.databinding.ActivityObjectsBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ObjectsActivity : AppCompatActivity() {

    private var binding: ActivityObjectsBinding? = null
    var context: Context = this@ObjectsActivity
    var list: ArrayList<OBJECT?>? = null
    var adapter: ObjectAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        THEME.setThemeOfApp(context)
        super.onCreate(savedInstanceState)
        binding = ActivityObjectsBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        binding!!.toolbar.nameSpace.setText(R.string.objects)
        binding!!.toolbar.back.setOnClickListener { onBackPressed() }
        binding!!.toolbar.close.setOnClickListener { onBackPressed() }
        binding!!.add.add.setText(R.string.add_object)
        binding!!.add.item.setOnClickListener { VOID.Intent1(context, CLASS.OBJECT_ADD) }

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
        adapter = ObjectAdapter(context, list!!)
        binding!!.recyclerView.adapter = adapter
    }

    private fun loadInfo() {
        val ref = FirebaseDatabase.getInstance().getReference(DATA.OBJECTS)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                list!!.clear()
                for (data in dataSnapshot.children) {
                    val item = data.getValue(OBJECT::class.java)!!
                    if (item.publisher == DATA.FirebaseUserUid) list!!.add(item)
                }
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
        loadInfo()
    }

    override fun onRestart() {
        super.onRestart()
        loadInfo()
    }
}