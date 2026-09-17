package com.flatcode.littletasks.ui.objects

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littletasks.utils.DATA
import com.flatcode.littletasks.filter.ObjectOptionFilter
import com.flatcode.littletasks.model.TaskItem
import com.flatcode.littletasks.databinding.ItemObjectBinding
import java.text.MessageFormat

class ObjectOptionAdapter(
    private val context: Context,
    var list: ArrayList<TaskItem?>,
    var planId: String?,
    private val listener: ObjectOptionListener
) : RecyclerView.Adapter<ObjectOptionAdapter.ViewHolder>(), Filterable {

    interface ObjectOptionListener {
        fun onOptionClick(item: TaskItem)
        fun isPlan(objectId: String, planId: String, imageView: ImageView)
    }

    var filterList: ArrayList<TaskItem?> = list
    private var filter: ObjectOptionFilter? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemObjectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position] ?: return
        val id = DATA.EMPTY + item.id
        val name = DATA.EMPTY + item.name
        val points = DATA.EMPTY + item.points

        holder.binding.option.visibility = View.VISIBLE
        holder.binding.more.visibility = View.GONE

        if (name == DATA.EMPTY) {
            holder.binding.name.visibility = View.GONE
        } else {
            holder.binding.name.visibility = View.VISIBLE
            holder.binding.name.text = name
        }

        if (points == DATA.EMPTY) {
            holder.binding.points.text = MessageFormat.format("{0}{1}", DATA.EMPTY, DATA.ZERO)
        } else {
            holder.binding.points.text = points
        }

        listener.isPlan(id, planId ?: "", holder.binding.option)
        holder.binding.option.setOnClickListener { listener.onOptionClick(item) }
    }

    override fun getItemCount(): Int = list.size

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = ObjectOptionFilter(filterList, this)
        }
        return filter!!
    }

    class ViewHolder(val binding: ItemObjectBinding) : RecyclerView.ViewHolder(binding.root)
}
