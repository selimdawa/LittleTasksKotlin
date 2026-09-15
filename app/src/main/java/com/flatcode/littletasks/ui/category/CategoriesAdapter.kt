package com.flatcode.littletasks.ui.category

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littletasks.core.utils.DATA
import com.flatcode.littletasks.core.utils.loadImage
import com.flatcode.littletasks.core.utils.moreCategory
import com.flatcode.littletasks.core.utils.openActivity
import com.flatcode.littletasks.core.utils.filter.CategoriesFilter
import com.flatcode.littletasks.data.model.Category
import com.flatcode.littletasks.data.model.Task
import com.flatcode.littletasks.databinding.ItemCategoriesBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.MessageFormat

class CategoriesAdapter(private val context: Context, var list: ArrayList<Category?>) :
    RecyclerView.Adapter<CategoriesAdapter.ViewHolder>(), Filterable {

    var filterList: ArrayList<Category?> = list
    private var filter: CategoriesFilter? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoriesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

        nrBooks(holder.binding.number, id)
        holder.binding.more.setOnClickListener { context.moreCategory(item) }

        holder.binding.card.setOnClickListener {
            context.openActivity(CategoryTasksActivity::class.java, DATA.ID to id, DATA.NAME to name)
        }
    }

    override fun getItemCount(): Int = list.size

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = CategoriesFilter(filterList, this)
        }
        return filter!!
    }

    class ViewHolder(val binding: ItemCategoriesBinding) : RecyclerView.ViewHolder(binding.root)

    companion object {
        fun nrBooks(number: TextView, categoryId: String) {
            val reference = FirebaseDatabase.getInstance().getReference(DATA.TASKS)
            reference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var i = 0
                    for (snapshot in dataSnapshot.children) {
                        val item = snapshot.getValue(Task::class.java) ?: continue
                        if (item.category == categoryId) i++
                    }
                    number.text = MessageFormat.format("{0}{1}", DATA.EMPTY, i)
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }
    }
}
