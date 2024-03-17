package com.flatcode.littletasks.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littletasks.Filter.CategoriesFilter
import com.flatcode.littletasks.Model.Category
import com.flatcode.littletasks.Model.Task
import com.flatcode.littletasks.Unit.CLASS
import com.flatcode.littletasks.Unit.DATA
import com.flatcode.littletasks.Unit.VOID
import com.flatcode.littletasks.databinding.ItemCategoriesBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.MessageFormat

class CategoriesAdapter(private val context: Context, var list: ArrayList<Category?>) :
    RecyclerView.Adapter<CategoriesAdapter.ViewHolder>(), Filterable {

    private var binding: ItemCategoriesBinding? = null
    var filterList: ArrayList<Category?>
    private var filter: CategoriesFilter? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = ItemCategoriesBinding.inflate(LayoutInflater.from(context), parent, false)
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

        nrBooks(holder.numer, id)
        holder.more.setOnClickListener { VOID.moreCategory(context, item) }

        holder.card.setOnClickListener {
            VOID.IntentExtra2(context, CLASS.CATEGORY_TASKS, DATA.ID, id, DATA.NAME, name)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = CategoriesFilter(filterList, this)
        }
        return filter!!
    }

    inner class ViewHolder(view: View?) : RecyclerView.ViewHolder(view!!) {
        var image: ImageView
        var more: ImageView
        var name: TextView
        var numer: TextView
        var card: CardView

        init {
            image = binding!!.image
            name = binding!!.name
            more = binding!!.more
            numer = binding!!.numer
            card = binding!!.card
        }
    }

    companion object {
        fun nrBooks(number: TextView, categoryId: String) {
            val reference = FirebaseDatabase.getInstance().getReference(DATA.TASKS)
            reference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var i = 0
                    for (snapshot in dataSnapshot.children) {
                        val item = snapshot.getValue(Task::class.java)!!
                        if (item.category == categoryId) i++
                    }
                    number.text = MessageFormat.format("{0}{1}", DATA.EMPTY, i)
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }
    }

    init {
        filterList = list
    }
}