package com.flatcode.littletasks.ui.task

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littletasks.R
import com.flatcode.littletasks.databinding.ItemTaskBinding
import com.flatcode.littletasks.filter.TaskCategoryFilter
import com.flatcode.littletasks.model.Task
import com.flatcode.littletasks.utils.DATA
import com.flatcode.littletasks.utils.GetTimeAgo
import com.flatcode.littletasks.utils.loadImage

class TaskAdapter(
    private val listener: TaskListener
) : ListAdapter<Task, TaskAdapter.ViewHolder>(TaskDiffCallback()), Filterable {

    interface TaskListener {
        fun onMoreClick(item: Task)
        fun onFavoriteClick(item: Task)
        fun onTaskClick(item: Task)
        fun isFavorite(taskId: String, userId: String, imageView: ImageView)
    }

    var fullList = ArrayList<Task?>()
    private var filter: TaskCategoryFilter? = null

    fun setFullList(newList: List<Task?>) {
        fullList = ArrayList(newList)
        submitList(newList.filterNotNull())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position) ?: return
        holder.bind(item, listener)
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = TaskCategoryFilter(fullList, this)
        }
        return filter!!
    }

    class ViewHolder(val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Task, listener: TaskListener) {
            val id = DATA.EMPTY + item.id
            val name = DATA.EMPTY + item.name
            val publisher = DATA.EMPTY + item.publisher
            val categoryName = DATA.EMPTY + item.categoryName
            val categoryImage = DATA.EMPTY + item.categoryImage
            val timestamp = DATA.EMPTY + item.timestamp
            val start = DATA.EMPTY + item.start
            val end = DATA.EMPTY + item.end
            val points = DATA.EMPTY + item.points
            val avPoints = DATA.EMPTY + item.aVPoints

            binding.name.text = name
            binding.name.visibility = if (name.isEmpty()) View.GONE else View.VISIBLE

            binding.points.text = points
            binding.AVPoints.text = avPoints

            val addTime = timestamp.toLongOrNull() ?: 0L
            binding.add.text = GetTimeAgo.getMessageAgo(addTime)

            binding.start.text =
                if (start == "0") "-" else GetTimeAgo.getMessageAgo(start.toLongOrNull() ?: 0L)
            binding.end.text =
                if (end == "0") "-" else GetTimeAgo.getMessageAgo(end.toLongOrNull() ?: 0L)

            // UI Updates for task status stars
            when {
                item.end != 0L -> binding.task.setImageResource(R.drawable.ic_star_selected)
                item.start != 0L -> binding.task.setImageResource(R.drawable.ic_star_half)
                else -> binding.task.setImageResource(R.drawable.ic_star_unselected)
            }

            binding.category.text = categoryName
            binding.image.loadImage(false, categoryImage)

            listener.isFavorite(id, publisher, binding.favorites)

            binding.favorites.setOnClickListener { listener.onFavoriteClick(item) }
            binding.task.setOnClickListener { listener.onTaskClick(item) }
            binding.more.setOnClickListener { listener.onMoreClick(item) }
        }
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }
}
