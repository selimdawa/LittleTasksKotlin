package com.flatcode.littletasks.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littletasks.Filter.ObjectsFilter
import com.flatcode.littletasks.Model.OBJECT
import com.flatcode.littletasks.Unit.DATA
import com.flatcode.littletasks.Unit.VOID
import com.flatcode.littletasks.databinding.ItemObjectBinding
import java.text.MessageFormat

class ObjectAdapter(private val context: Context, var list: ArrayList<OBJECT?>) :
    RecyclerView.Adapter<ObjectAdapter.ViewHolder>(), Filterable {

    private var binding: ItemObjectBinding? = null
    var filterList: ArrayList<OBJECT?>
    private var filter: ObjectsFilter? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = ItemObjectBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding!!.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val id = DATA.EMPTY + item!!.id
        val name = DATA.EMPTY + item.name
        val points = DATA.EMPTY + item.points

        if (name == DATA.EMPTY) {
            holder.name.visibility = View.GONE
        } else {
            holder.name.visibility = View.VISIBLE
            holder.name.text = name
        }

        if (points == DATA.EMPTY) {
            holder.points.text = MessageFormat.format("{0}{1}", DATA.EMPTY, DATA.ZERO)
        } else {
            holder.points.text = points
        }

        holder.more.setOnClickListener { VOID.moreObject(context, item) }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = ObjectsFilter(filterList, this)
        }
        return filter!!
    }

    inner class ViewHolder(view: View?) : RecyclerView.ViewHolder(view!!) {
        var name: TextView
        var points: TextView
        var more: ImageView
        var item: LinearLayout

        init {
            points = binding!!.points
            name = binding!!.name
            more = binding!!.more
            item = binding!!.item
        }
    }

    init {
        filterList = list
    }
}