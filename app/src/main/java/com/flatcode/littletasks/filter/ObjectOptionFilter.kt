package com.flatcode.littletasks.filter

import android.widget.Filter
import com.flatcode.littletasks.ui.objects.ObjectOptionAdapter
import com.flatcode.littletasks.model.TaskItem
import java.util.*

class ObjectOptionFilter(var list: ArrayList<TaskItem?>, var adapter: ObjectOptionAdapter) : Filter() {

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        val results = FilterResults()
        if (!constraint.isNullOrEmpty()) {
            val query = constraint.toString().uppercase(Locale.getDefault())
            val filteredList = ArrayList<TaskItem?>()
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
        val newList = results.values as ArrayList<TaskItem?>
        adapter.submitList(newList.filterNotNull())
    }
}