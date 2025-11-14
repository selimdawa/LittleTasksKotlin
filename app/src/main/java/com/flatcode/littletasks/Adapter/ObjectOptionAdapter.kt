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
import com.flatcode.littletasks.Filter.ObjectOptionFilter
import com.flatcode.littletasks.Model.OBJECT
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
    private val context: Context, var list: ArrayList<OBJECT?>, planId: String?
) : RecyclerView.Adapter<ObjectOptionAdapter.ViewHolder>(), Filterable {

    private var binding: ItemObjectBinding? = null
    var filterList: ArrayList<OBJECT?>
    private var filter: ObjectOptionFilter? = null
    var planId: String?

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = ItemObjectBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding!!.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val id = DATA.EMPTY + item!!.id
        val name = DATA.EMPTY + item.name
        val points = DATA.EMPTY + item.points

        holder.option.visibility = View.VISIBLE
        holder.more.visibility = View.GONE

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

        VOID.isPlan(holder.option, id, planId)
        holder.option.setOnClickListener { VOID.checkPlan(holder.option, id, planId) }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = ObjectOptionFilter(filterList, this)
        }
        return filter!!
    }

    inner class ViewHolder(view: View?) : RecyclerView.ViewHolder(view!!) {
        var name: TextView
        var points: TextView
        var more: ImageView
        var option: ImageView
        var item: LinearLayout

        init {
            points = binding!!.points
            name = binding!!.name
            more = binding!!.more
            option = binding!!.option
            item = binding!!.item
        }
    }

    companion object {
        fun isLoves(image: ImageView, planId: String?, objectId: String?) {
            val reference = FirebaseDatabase.getInstance().getReference(DATA.PLANS).child(planId!!)
                .child(DATA.OBJECTS)
            reference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.child(objectId!!).exists()) {
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
            if (image.tag == "add") {
                FirebaseDatabase.getInstance().getReference(DATA.PLANS).child(planId!!)
                    .child(DATA.OBJECTS).child(objectId!!).setValue(true)
            } else {
                FirebaseDatabase.getInstance().getReference(DATA.PLANS).child(planId!!)
                    .child(DATA.OBJECTS).child(objectId!!).removeValue()
            }
        }
    }

    init {
        filterList = list
        this.planId = planId
    }
}