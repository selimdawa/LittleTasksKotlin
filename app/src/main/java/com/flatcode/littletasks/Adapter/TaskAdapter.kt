package com.flatcode.littletasks.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littletasks.Filter.TaskCategoryFilter
import com.flatcode.littletasks.Model.Category
import com.flatcode.littletasks.Model.Task
import com.flatcode.littletasks.Unit.DATA
import com.flatcode.littletasks.Unit.GetTimeAgo
import com.flatcode.littletasks.Unit.VOID
import com.flatcode.littletasks.databinding.ItemTaskBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TaskAdapter(private val context: Context, var list: ArrayList<Task?>) :
    RecyclerView.Adapter<TaskAdapter.ViewHolder>(), Filterable {

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

        if (name == DATA.EMPTY) {
            holder.binding.name.visibility = View.GONE
        } else {
            holder.binding.name.visibility = View.VISIBLE
            holder.binding.name.text = name
        }

        holder.binding.points.text = points
        holder.binding.AVPoints.text = avPoints

        val addTime = timestamp.toLongOrNull() ?: 0L
        holder.binding.add.text = GetTimeAgo.getMessageAgo(addTime)

        if (start == "0") {
            holder.binding.start.text = "-"
        } else {
            val startTime = start.toLongOrNull() ?: 0L
            holder.binding.start.text = GetTimeAgo.getMessageAgo(startTime)
        }

        if (end == "0") {
            holder.binding.end.text = "-"
        } else {
            val endTime = end.toLongOrNull() ?: 0L
            holder.binding.end.text = GetTimeAgo.getMessageAgo(endTime)
        }

        getData(category, holder.binding.category, holder.binding.image)
        VOID.isFavorite(holder.binding.favorites, id, publisher)
        holder.binding.favorites.setOnClickListener { VOID.checkFavorite(holder.binding.favorites, id) }
        VOID.isTask(context, holder.binding.task, id)

        holder.binding.more.setOnClickListener { VOID.moreTask(context, item) }
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
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val item = dataSnapshot.getValue(Category::class.java) ?: return
                    name.text = item.name
                    VOID.GlideImage(false, context, item.image, image)
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }
}