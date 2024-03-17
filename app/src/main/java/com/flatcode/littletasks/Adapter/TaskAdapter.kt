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
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class TaskAdapter(private val context: Context, var list: ArrayList<Task?>) :
    RecyclerView.Adapter<TaskAdapter.ViewHolder>(), Filterable {

    private var binding: ItemTaskBinding? = null
    var filterList: ArrayList<Task?>
    private var filter: TaskCategoryFilter? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = ItemTaskBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding!!.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val id = DATA.EMPTY + item!!.id
        val name = DATA.EMPTY + item.name
        val publisher = DATA.EMPTY + item.publisher
        val category = DATA.EMPTY + item.category
        val timestamp = DATA.EMPTY + item.timestamp
        val start = DATA.EMPTY + item.start
        val end = DATA.EMPTY + item.end
        val points = DATA.EMPTY + item.points
        val AVPoints = DATA.EMPTY + item.aVPoints

        if (name == DATA.EMPTY) {
            holder.name.visibility = View.GONE
        } else {
            holder.name.visibility = View.VISIBLE
            holder.name.text = name
        }

        holder.points.text = points
        holder.AVPoints.text = AVPoints
        val addTime = timestamp.toLong()
        val Add = GetTimeAgo.getMessageAgo(addTime, context)
        holder.add.text = Add

        if (start == "0") {
            holder.start.text = "-"
        } else {
            val startTime = start.toLong()
            val Start = GetTimeAgo.getMessageAgo(startTime, context)
            holder.start.text = Start
        }

        if (end == "0") {
            holder.end.text = "-"
        } else {
            val endTime = end.toLong()
            val End = GetTimeAgo.getMessageAgo(endTime, context)
            holder.end.text = End
        }

        getData(category, holder.category, holder.image)
        VOID.isFavorite(holder.favorites, id, publisher)
        holder.favorites.setOnClickListener { VOID.checkFavorite(holder.favorites, id) }
        VOID.isTask(context, holder.task, id)

        holder.more.setOnClickListener { VOID.moreTask(context, item) }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = TaskCategoryFilter(filterList, this)
        }
        return filter!!
    }

    inner class ViewHolder(view: View?) : RecyclerView.ViewHolder(view!!) {
        var image: ImageView
        var favorites: ImageView
        var task: ImageView
        var more: ImageView
        var name: TextView
        var category: TextView
        var points: TextView
        var AVPoints: TextView
        var add: TextView
        var start: TextView
        var end: TextView
        var item: LinearLayout

        init {
            image = binding!!.image
            favorites = binding!!.favorites
            task = binding!!.task
            name = binding!!.name
            category = binding!!.category
            points = binding!!.points
            AVPoints = binding!!.AVPoints
            add = binding!!.add
            start = binding!!.start
            end = binding!!.end
            more = binding!!.more
            item = binding!!.item
        }
    }

    private fun getData(categoryId: String, name: TextView, image: ImageView) {
        val reference: Query =
            FirebaseDatabase.getInstance().getReference(DATA.CATEGORIES).child(categoryId)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val item = dataSnapshot.getValue(Category::class.java)!!
                name.text = item.name
                VOID.GlideImage(false, context, item.image, image)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    init {
        filterList = list
    }
}