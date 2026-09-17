package com.flatcode.littletasks.filter

import android.widget.Filter
import com.flatcode.littletasks.ui.plan.PlanAdapter
import com.flatcode.littletasks.model.Plan
import java.util.*

class PlansFilter(var list: ArrayList<Plan?>, var adapter: PlanAdapter) : Filter() {

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        val results = FilterResults()
        if (!constraint.isNullOrEmpty()) {
            val query = constraint.toString().uppercase(Locale.getDefault())
            val filteredList = ArrayList<Plan?>()
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
        val newList = results.values as ArrayList<Plan?>
        adapter.submitList(newList.filterNotNull())
    }
}