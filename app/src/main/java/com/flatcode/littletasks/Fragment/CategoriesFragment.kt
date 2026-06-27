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
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase

class CategoriesFragment : Fragment() {

    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!

    private val list = ArrayList<Category?>()
    private var adapter: CategoryMainAdapter? = null
    private var childEventListener: ChildEventListener? = null
    private val databaseRef = FirebaseDatabase.getInstance().getReference(DATA.CATEGORIES)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)

        adapter = CategoryMainAdapter(context, list)
        binding.recyclerView.adapter = adapter

        return binding.root
    }

    private fun observeItems() {
        val uid = DATA.FirebaseUserUid
        if (childEventListener != null) return

        val previousSize = list.size
        if (previousSize > 0) {
            list.clear()
            adapter?.notifyItemRangeRemoved(0, previousSize)
        }

        childEventListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val item = snapshot.getValue(Category::class.java) ?: return
                if (item.publisher == uid) {
                    list.add(item)
                    val insertedIndex = list.size - 1
                    adapter?.notifyItemInserted(insertedIndex)
                    updateViewsVisibility()
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val item = snapshot.getValue(Category::class.java) ?: return
                val index = list.indexOfFirst { it?.id == item.id }

                if (item.publisher == uid) {
                    if (index != -1) {
                        list[index] = item
                        adapter?.notifyItemChanged(index)
                    } else {
                        list.add(item)
                        adapter?.notifyItemInserted(list.size - 1)
                        updateViewsVisibility()
                    }
                } else if (index != -1) {
                    list.removeAt(index)
                    adapter?.notifyItemRemoved(index)
                    updateViewsVisibility()
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val item = snapshot.getValue(Category::class.java) ?: return
                val index = list.indexOfFirst { it?.id == item.id }
                if (index != -1) {
                    list.removeAt(index)
                    adapter?.notifyItemRemoved(index)
                    updateViewsVisibility()
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {}
        }

        binding.bar.visibility = View.GONE
        databaseRef.addChildEventListener(childEventListener!!)
    }

    private fun updateViewsVisibility() {
        _binding?.let { b ->
            if (list.isNotEmpty()) {
                b.recyclerView.visibility = View.VISIBLE
                b.emptyText.visibility = View.GONE
            } else {
                b.recyclerView.visibility = View.GONE
                b.emptyText.visibility = View.VISIBLE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        observeItems()
    }

    override fun onPause() {
        super.onPause()
        childEventListener?.let {
            databaseRef.removeEventListener(it)
            childEventListener = null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}