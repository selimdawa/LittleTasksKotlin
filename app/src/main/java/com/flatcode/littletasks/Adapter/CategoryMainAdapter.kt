package com.flatcode.littletasks.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littletasks.Model.Category
import com.flatcode.littletasks.Unit.CLASS
import com.flatcode.littletasks.Unit.DATA
import com.flatcode.littletasks.Unit.VOID
import com.flatcode.littletasks.databinding.ItemCategoryBinding

class CategoryMainAdapter(private val context: Context?, var list: ArrayList<Category?>) :
    RecyclerView.Adapter<CategoryMainAdapter.ViewHolder>() {

    private var binding: ItemCategoryBinding? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = ItemCategoryBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding!!.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val id = DATA.EMPTY + item!!.id
        val name = DATA.EMPTY + item.name
        val image = DATA.EMPTY + item.image

        VOID.GlideImage(false, context, image, holder.image)
        VOID.GlideBlur(false, context, image, holder.imageBlur, 50)

        if (name == DATA.EMPTY) {
            holder.name.visibility = View.GONE
        } else {
            holder.name.visibility = View.VISIBLE
            holder.name.text = name
        }

        holder.more.setOnClickListener { VOID.moreCategory(context, item) }
        holder.card.setOnClickListener {
            VOID.IntentExtra2(context, CLASS.CATEGORY_TASKS, DATA.ID, id, DATA.NAME, name)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(view: View?) : RecyclerView.ViewHolder(view!!) {
        var image: ImageView
        var imageBlur: ImageView
        var more: ImageView
        var name: TextView
        var card: CardView

        init {
            image = binding!!.image
            imageBlur = binding!!.imageBlur
            name = binding!!.name
            more = binding!!.more
            card = binding!!.card
        }
    }
}