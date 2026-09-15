package com.flatcode.littletasks.ui.category

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littletasks.core.utils.DATA
import com.flatcode.littletasks.core.utils.loadBlurImage
import com.flatcode.littletasks.core.utils.loadImage
import com.flatcode.littletasks.core.utils.openActivity
import com.flatcode.littletasks.data.model.Category
import com.flatcode.littletasks.databinding.ItemCategoryBinding

class CategoryMainAdapter(
    private val context: Context?,
    var list: ArrayList<Category?>,
    private val listener: CategoryMainListener
) : RecyclerView.Adapter<CategoryMainAdapter.ViewHolder>() {

    interface CategoryMainListener {
        fun onMoreClick(item: Category)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position] ?: return
        val id = DATA.EMPTY + item.id
        val name = DATA.EMPTY + item.name
        val image = DATA.EMPTY + item.image

        holder.binding.image.loadImage(false, image)
        holder.binding.imageBlur.loadBlurImage(false, image, 50)

        if (name == DATA.EMPTY) {
            holder.binding.name.visibility = View.GONE
        } else {
            holder.binding.name.visibility = View.VISIBLE
            holder.binding.name.text = name
        }

        holder.binding.more.setOnClickListener { listener.onMoreClick(item) }
        holder.binding.card.setOnClickListener {
            context?.openActivity(CategoryTasksActivity::class.java, DATA.ID to id, DATA.NAME to name)
        }
    }

    override fun getItemCount(): Int = list.size

    class ViewHolder(val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root)
}
