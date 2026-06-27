package com.flatcode.littletasks.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littletasks.Filter.ObjectsFilter
import com.flatcode.littletasks.Model.TaskItem
import com.flatcode.littletasks.Unit.DATA
import com.flatcode.littletasks.Unit.VOID
import com.flatcode.littletasks.databinding.ItemObjectBinding
import java.text.MessageFormat

class ObjectAdapter(private val context: Context, var list: ArrayList<TaskItem?>) :
    RecyclerView.Adapter<ObjectAdapter.ViewHolder>(), Filterable {

    var filterList: ArrayList<TaskItem?> = list
    private var filter: ObjectsFilter? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemObjectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position] ?: return
        val name = DATA.EMPTY + item.name
        val points = DATA.EMPTY + item.points

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

        holder.binding.more.setOnClickListener { VOID.moreObject(context, item) }
    }

    override fun getItemCount(): Int = list.size

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = ObjectsFilter(filterList, this)
        }
        return filter!!
    }

    class ViewHolder(val binding: ItemObjectBinding) : RecyclerView.ViewHolder(binding.root)
}