package com.flatcode.littletasks.ui.objects

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littletasks.databinding.ItemObjectBinding
import com.flatcode.littletasks.model.TaskItem
import com.flatcode.littletasks.utils.DATA
import java.util.Locale

class ObjectOptionAdapter(
    var planId: String?, private val listener: ObjectOptionListener
) : ListAdapter<TaskItem, ObjectOptionAdapter.ViewHolder>(ObjectDiffCallback()) {

    interface ObjectOptionListener {
        fun onOptionClick(item: TaskItem)
        fun isPlan(objectId: String, planId: String, imageView: ImageView)
    }

    private var fullList = listOf<TaskItem>()

    fun setFullList(newList: List<TaskItem?>) {
        fullList = newList.filterNotNull()
        submitList(fullList)
    }

    fun filter(query: CharSequence?) {
        val list = if (query.isNullOrEmpty()) {
            fullList
        } else {
            val constraint = query.toString().uppercase(Locale.getDefault())
            fullList.filter { it.name?.uppercase(Locale.getDefault())?.contains(constraint) == true }
        }
        submitList(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemObjectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position) ?: return
        holder.bind(item, planId, listener)
    }

    class ViewHolder(val binding: ItemObjectBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TaskItem, planId: String?, listener: ObjectOptionListener) {
            val id = DATA.EMPTY + item.id
            val name = DATA.EMPTY + item.name
            val points = DATA.EMPTY + item.points

            binding.option.visibility = View.VISIBLE
            binding.more.visibility = View.GONE

            if (name == DATA.EMPTY) {
                binding.name.visibility = View.GONE
            } else {
                binding.name.visibility = View.VISIBLE
                binding.name.text = name
            }

            binding.points.text = points

            listener.isPlan(id, planId ?: "", binding.option)
            binding.option.setOnClickListener { listener.onOptionClick(item) }
        }
    }

    class ObjectDiffCallback : DiffUtil.ItemCallback<TaskItem>() {
        override fun areItemsTheSame(oldItem: TaskItem, newItem: TaskItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TaskItem, newItem: TaskItem): Boolean {
            return oldItem == newItem
        }
    }
}
