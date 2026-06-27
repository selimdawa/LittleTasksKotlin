package com.flatcode.littletasks.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littletasks.Filter.ObjectOptionFilter
import com.flatcode.littletasks.Model.TaskItem
import com.flatcode.littletasks.R
import com.flatcode.littletasks.Unit.DATA
import com.flatcode.littletasks.Unit.VOID
import com.flatcode.littletasks.databinding.ItemObjectBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.MessageFormat

class ObjectOptionAdapter(
    private val context: Context, var list: ArrayList<TaskItem?>, var planId: String?
) : RecyclerView.Adapter<ObjectOptionAdapter.ViewHolder>(), Filterable {

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

        VOID.isPlan(holder.binding.option, id, planId)
        holder.binding.option.setOnClickListener { VOID.checkPlan(holder.binding.option, id, planId) }
    }

    override fun getItemCount(): Int = list.size

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = ObjectOptionFilter(filterList, this)
        }
        return filter!!
    }

    class ViewHolder(val binding: ItemObjectBinding) : RecyclerView.ViewHolder(binding.root)

    companion object {
        fun isLoves(image: ImageView, planId: String?, objectId: String?) {
            if (planId == null || objectId == null) return
            val reference = FirebaseDatabase.getInstance().getReference(DATA.PLANS).child(planId)
                .child(DATA.OBJECTS)
            reference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.child(objectId).exists()) {
                        image.setImageResource(R.drawable.ic_remove)
                        image.tag = "added"
                    } else {
                        image.setImageResource(R.drawable.ic___add)
                        image.tag = "add"
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }

        fun addToPlan(image: ImageView, planId: String?, objectId: String?) {
            if (planId == null || objectId == null) return
            val reference = FirebaseDatabase.getInstance().getReference(DATA.PLANS).child(planId)
                .child(DATA.OBJECTS).child(objectId)
            if (image.tag == "add") {
                reference.setValue(true)
            } else {
                reference.removeValue()
            }
        }
    }
}