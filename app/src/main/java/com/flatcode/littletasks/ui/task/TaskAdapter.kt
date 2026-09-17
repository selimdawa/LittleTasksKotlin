package com.flatcode.littletasks.ui.task

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littletasks.R
import com.flatcode.littletasks.utils.DATA
import com.flatcode.littletasks.utils.GetTimeAgo
import com.flatcode.littletasks.utils.loadImage
import com.flatcode.littletasks.filter.TaskCategoryFilter
import com.flatcode.littletasks.model.Category
import com.flatcode.littletasks.model.Task
import com.flatcode.littletasks.databinding.ItemTaskBinding
import com.google.firebase.database.*

class TaskAdapter(
    private val context: Context,
    var list: ArrayList<Task?>,
    private val listener: TaskListener
) : RecyclerView.Adapter<TaskAdapter.ViewHolder>(), Filterable {

    interface TaskListener {
        fun onMoreClick(item: Task)
        fun onFavoriteClick(item: Task)
        fun onTaskClick(item: Task)
        fun isFavorite(taskId: String, userId: String, imageView: ImageView)
    }

    var filterList: ArrayList<Task?> = list
    private var filter: TaskCategoryFilter? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position] ?: return
        val id = DATA.EMPTY + item.id
        val name = DATA.EMPTY + item.name
        val publisher = DATA.EMPTY + item.publisher
        val category = DATA.EMPTY + item.category
        val timestamp = DATA.EMPTY + item.timestamp
        val start = DATA.EMPTY + item.start
        val end = DATA.EMPTY + item.end
        val points = DATA.EMPTY + item.points
        val avPoints = DATA.EMPTY + item.aVPoints

        holder.binding.name.text = name
        holder.binding.name.visibility = if (name.isEmpty()) View.GONE else View.VISIBLE
        
        holder.binding.points.text = points
        holder.binding.AVPoints.text = avPoints

        val addTime = timestamp.toLongOrNull() ?: 0L
        holder.binding.add.text = GetTimeAgo.getMessageAgo(addTime)

        holder.binding.start.text = if (start == "0") "-" else GetTimeAgo.getMessageAgo(start.toLongOrNull() ?: 0L)
        holder.binding.end.text = if (end == "0") "-" else GetTimeAgo.getMessageAgo(end.toLongOrNull() ?: 0L)

        // UI Updates for task status stars
        when {
            item.end != 0L -> holder.binding.task.setImageResource(R.drawable.ic_star_selected)
            item.start != 0L -> holder.binding.task.setImageResource(R.drawable.ic_star_half)
            else -> holder.binding.task.setImageResource(R.drawable.ic_star_unselected)
        }

        getData(category, holder.binding.category, holder.binding.image)
        
        listener.isFavorite(id, publisher, holder.binding.favorites)
        
        holder.binding.favorites.setOnClickListener { listener.onFavoriteClick(item) }
        holder.binding.task.setOnClickListener { listener.onTaskClick(item) }
        holder.binding.more.setOnClickListener { listener.onMoreClick(item) }
    }

    override fun getItemCount(): Int = list.size

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = TaskCategoryFilter(filterList, this)
        }
        return filter!!
    }

    class ViewHolder(val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root)

    private fun getData(categoryId: String, name: TextView, image: ImageView) {
        FirebaseDatabase.getInstance().getReference(DATA.CATEGORIES).child(categoryId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val item = dataSnapshot.getValue(Category::class.java) ?: return
                    name.text = item.name
                    image.loadImage(false, item.image)
                }
                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }
}
