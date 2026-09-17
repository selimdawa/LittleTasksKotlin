package com.flatcode.littletasks.filter

import android.widget.Filter
import com.flatcode.littletasks.ui.category.CategoriesAdapter
import com.flatcode.littletasks.model.Category
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
        val newList = results.values as? ArrayList<Category?> ?: ArrayList()
        adapter.submitList(newList.filterNotNull())
    }
}