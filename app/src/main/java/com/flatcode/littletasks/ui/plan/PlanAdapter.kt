package com.flatcode.littletasks.ui.plan

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littletasks.utils.DATA
import com.flatcode.littletasks.utils.loadImage
import com.flatcode.littletasks.utils.openActivity
import com.flatcode.littletasks.filter.PlansFilter
import com.flatcode.littletasks.model.Plan
import com.flatcode.littletasks.databinding.ItemPlanBinding
import com.flatcode.littletasks.ui.category.CategoryAddActivity
import com.flatcode.littletasks.ui.objects.ObjectsPlanActivity

class PlanAdapter(
    private val context: Context,
    var list: ArrayList<Plan?>,
    var isNew: Boolean,
    private val listener: PlanListener
) : RecyclerView.Adapter<PlanAdapter.ViewHolder>(), Filterable {

    interface PlanListener {
        fun onMoreClick(item: Plan)
    }

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

        holder.binding.image.loadImage(false, image)

        if (name == DATA.EMPTY) {
            holder.binding.name.visibility = View.GONE
        } else {
            holder.binding.name.visibility = View.VISIBLE
            holder.binding.name.text = name
        }

        holder.binding.more.visibility = if (isNew) View.GONE else View.VISIBLE

        holder.binding.more.setOnClickListener { listener.onMoreClick(item) }
        holder.binding.item.setOnClickListener {
            if (isNew) {
                context.openActivity(CategoryAddActivity::class.java, DATA.ID to id)
            } else {
                context.openActivity(ObjectsPlanActivity::class.java, DATA.ID to id, DATA.NAME to name)
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
