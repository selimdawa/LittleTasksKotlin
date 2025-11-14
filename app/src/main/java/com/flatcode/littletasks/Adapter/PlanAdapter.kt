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
import com.flatcode.littletasks.Filter.PlansFilter
import com.flatcode.littletasks.Model.Plan
import com.flatcode.littletasks.Unit.CLASS
import com.flatcode.littletasks.Unit.DATA
import com.flatcode.littletasks.Unit.VOID
import com.flatcode.littletasks.databinding.ItemPlanBinding

class PlanAdapter(private val context: Context, var list: ArrayList<Plan?>, isNew: Boolean) :
    RecyclerView.Adapter<PlanAdapter.ViewHolder>(), Filterable {

    private var binding: ItemPlanBinding? = null
    var filterList: ArrayList<Plan?>
    private var filter: PlansFilter? = null
    var isNew: Boolean

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = ItemPlanBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding!!.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val id = DATA.EMPTY + item!!.id
        val name = DATA.EMPTY + item.name
        val image = DATA.EMPTY + item.image

        VOID.GlideImage(false, context, image, holder.image)

        if (name == DATA.EMPTY) {
            holder.name.visibility = View.GONE
        } else {
            holder.name.visibility = View.VISIBLE
            holder.name.text = name
        }

        if (isNew) {
            holder.more.visibility = View.GONE
        } else {
            holder.more.visibility = View.VISIBLE
        }

        holder.more.setOnClickListener { VOID.morePlan(context, item) }
        holder.item.setOnClickListener {
            if (isNew) VOID.IntentExtra(
                context, CLASS.CATEGORY_ADD, DATA.ID, id
            ) else VOID.IntentExtra2(
                context, CLASS.OBJECTS_PLAN, DATA.ID, id, DATA.NAME, name
            )
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = PlansFilter(filterList, this)
        }
        return filter!!
    }

    inner class ViewHolder(view: View?) : RecyclerView.ViewHolder(view!!) {
        var image: ImageView
        var more: ImageView
        var name: TextView
        var item: LinearLayout

        init {
            image = binding!!.image
            name = binding!!.name
            more = binding!!.more
            item = binding!!.item
        }
    }

    init {
        filterList = list
        this.isNew = isNew
    }
}