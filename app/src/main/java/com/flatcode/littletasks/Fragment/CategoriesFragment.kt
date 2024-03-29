package com.flatcode.littletasks.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.flatcode.littletasks.Adapter.CategoryMainAdapter
import com.flatcode.littletasks.Model.Category
import com.flatcode.littletasks.Unit.DATA
import com.flatcode.littletasks.databinding.FragmentCategoriesBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class CategoriesFragment : Fragment() {

    private var binding: FragmentCategoriesBinding? = null
    var list: ArrayList<Category?>? = null
    var adapter: CategoryMainAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCategoriesBinding.inflate(LayoutInflater.from(context), container, false)

        //binding.recyclerCategory.setHasFixedSize(true);
        list = ArrayList()
        adapter = CategoryMainAdapter(context, list!!)
        binding!!.recyclerView.adapter = adapter

        return binding!!.root
    }

    private fun loadItems() {
        val ref: Query = FirebaseDatabase.getInstance().getReference(DATA.CATEGORIES)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                list!!.clear()
                for (snapshot in dataSnapshot.children) {
                    val item = snapshot.getValue(Category::class.java)!!
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

    override fun onResume() {
        super.onResume()
        loadItems()
    }
}