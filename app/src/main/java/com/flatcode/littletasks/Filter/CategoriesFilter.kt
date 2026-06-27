package com.flatcode.littletasks.Filter

import android.widget.Filter
import com.flatcode.littletasks.Adapter.CategoriesAdapter
import com.flatcode.littletasks.Model.Category
import java.util.*

class CategoriesFilter(var list: ArrayList<Category?>, var adapter: CategoriesAdapter) : Filter() {

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        val results = FilterResults()
        if (!constraint.isNullOrEmpty()) {
            val query = constraint.toString().uppercase(Locale.getDefault())
            val filteredList = ArrayList<Category?>()
            for (item in list) {
                if (item?.name?.uppercase(Locale.getDefault())?.contains(query) == true) {
                    filteredList.add(item)
                }
            }
            results.count = filteredList.size
            results.values = filteredList
        } else {
            results.count = list.size
            results.values = list
        }
        return results
    }

    @Suppress("UNCHECKED_CAST")
    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        val oldList = adapter.list
        val newList = results.values as ArrayList<Category?>

        adapter.list = newList

        val oldSize = oldList.size
        val newSize = newList.size

        when {
            oldSize == 0 && newSize > 0 -> adapter.notifyItemRangeInserted(0, newSize)
            oldSize > 0 && newSize == 0 -> adapter.notifyItemRangeRemoved(0, oldSize)
            else -> {
                for (i in 0 until minOf(oldSize, newSize)) {
                    if (oldList[i] != newList[i]) {
                        adapter.notifyItemChanged(i)
                    }
                }
                if (newSize > oldSize) {
                    adapter.notifyItemRangeInserted(oldSize, newSize - oldSize)
                } else if (oldSize > newSize) {
                    adapter.notifyItemRangeRemoved(newSize, oldSize - newSize)
                }
            }
        }
    }
}