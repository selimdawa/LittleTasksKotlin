package com.flatcode.littletasks.ui.objects

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littletasks.utils.DATA
import com.flatcode.littletasks.filter.ObjectsFilter
import com.flatcode.littletasks.model.TaskItem
import com.flatcode.littletasks.databinding.ItemObjectBinding

class ObjectAdapter(
    private val listener: ObjectListener
) : ListAdapter<TaskItem, ObjectAdapter.ViewHolder>(ObjectDiffCallback()), Filterable {

    interface ObjectListener {
        fun onMoreClick(item: TaskItem)
    }

    var fullList = ArrayList<TaskItem?>()
    private var filter: ObjectsFilter? = null

    fun setFullList(newList: List<TaskItem?>) {
        fullList = ArrayList(newList)
        submitList(newList.filterNotNull())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemObjectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position) ?: return
        val name = DATA.EMPTY + item.name
        val points = DATA.EMPTY + item.points

        if (name == DATA.EMPTY) {
            holder.binding.name.visibility = View.GONE
        } else {
            holder.binding.name.visibility = View.VISIBLE
            holder.binding.name.text = name
        }

        holder.binding.points.text = points

        holder.binding.more.setOnClickListener { listener.onMoreClick(item) }
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = ObjectsFilter(fullList, this)
        }
        return filter!!
    }

    class ViewHolder(val binding: ItemObjectBinding) : RecyclerView.ViewHolder(binding.root)

    class ObjectDiffCallback : DiffUtil.ItemCallback<TaskItem>() {
        override fun areItemsTheSame(oldItem: TaskItem, newItem: TaskItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TaskItem, newItem: TaskItem): Boolean {
            return oldItem == newItem
        }
    }
}
