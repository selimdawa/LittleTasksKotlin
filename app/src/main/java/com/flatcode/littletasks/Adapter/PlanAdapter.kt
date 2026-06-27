package com.flatcode.littletasks.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littletasks.Filter.PlansFilter
import com.flatcode.littletasks.Model.Plan
import com.flatcode.littletasks.Unit.CLASS
import com.flatcode.littletasks.Unit.DATA
import com.flatcode.littletasks.Unit.VOID
import com.flatcode.littletasks.databinding.ItemPlanBinding

class PlanAdapter(private val context: Context, var list: ArrayList<Plan?>, var isNew: Boolean) :
    RecyclerView.Adapter<PlanAdapter.ViewHolder>(), Filterable {

    var filterList: ArrayList<Plan?> = list
    private var filter: PlansFilter? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPlanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position] ?: return
        val id = DATA.EMPTY + item.id
        val name = DATA.EMPTY + item.name
        val image = DATA.EMPTY + item.image

        VOID.GlideImage(false, context, image, holder.binding.image)

        if (name == DATA.EMPTY) {
            holder.binding.name.visibility = View.GONE
        } else {
            holder.binding.name.visibility = View.VISIBLE
            holder.binding.name.text = name
        }

        holder.binding.more.visibility = if (isNew) View.GONE else View.VISIBLE

        holder.binding.more.setOnClickListener { VOID.morePlan(context, item) }
        holder.binding.item.setOnClickListener {
            if (isNew) {
                VOID.IntentExtra(context, CLASS.CATEGORY_ADD, DATA.ID, id)
            } else {
                VOID.IntentExtra2(context, CLASS.OBJECTS_PLAN, DATA.ID, id, DATA.NAME, name)
            }
        }
    }

    override fun getItemCount(): Int = list.size

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = PlansFilter(filterList, this)
        }
        return filter!!
    }

    class ViewHolder(val binding: ItemPlanBinding) : RecyclerView.ViewHolder(binding.root)
}