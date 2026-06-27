package com.flatcode.littletasks.Activity

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littletasks.Adapter.CategoriesAdapter
import com.flatcode.littletasks.Model.Category
import com.flatcode.littletasks.R
import com.flatcode.littletasks.Unit.CLASS
import com.flatcode.littletasks.Unit.DATA
import com.flatcode.littletasks.Unit.THEME
import com.flatcode.littletasks.Unit.VOID
import com.flatcode.littletasks.databinding.ActivityPageStaggeredBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.MessageFormat

class CategoriesActivity : AppCompatActivity() {

    private var _binding: ActivityPageStaggeredBinding? = null
    private val binding get() = _binding!!

    private val context: Context = this@CategoriesActivity
    private val list = ArrayList<Category?>()
    private var adapter: CategoriesAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        THEME.setThemeOfApp(context)
        super.onCreate(savedInstanceState)
        _binding = ActivityPageStaggeredBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.nameSpace.setText(R.string.categories)
        binding.toolbar.back.setOnClickListener { handleBackPressed() }
        binding.toolbar.close.setOnClickListener { handleBackPressed() }
        binding.add.add.setText(R.string.add_category)
        binding.add.item.setOnClickListener {
            VOID.IntentExtra(context, CLASS.PLANS, DATA.NEW_PLAN, "true")
        }

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

        adapter = CategoriesAdapter(context, list)
        binding.recyclerView.adapter = adapter
    }

    private fun getCategories(orderBy: String) {
        val uid = DATA.FirebaseUserUid
        FirebaseDatabase.getInstance().getReference(DATA.CATEGORIES)
            .orderByChild(orderBy)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val newList = ArrayList<Category?>()
                    for (data in dataSnapshot.children) {
                        val item = data.getValue(Category::class.java) ?: continue
                        if (item.publisher == uid) {
                            newList.add(item)
                        }
                    }
                    newList.reverse()

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
        getCategories(DATA.NAME)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}