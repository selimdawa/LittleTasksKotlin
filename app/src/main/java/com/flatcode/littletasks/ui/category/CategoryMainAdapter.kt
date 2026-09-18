package com.flatcode.littletasks.ui.category

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littletasks.databinding.ItemCategoryBinding
import com.flatcode.littletasks.model.Category
import com.flatcode.littletasks.utils.DATA
import com.flatcode.littletasks.utils.loadBlurImage
import com.flatcode.littletasks.utils.loadImage
import com.flatcode.littletasks.utils.openActivity

class CategoryMainAdapter(
    private val listener: CategoryMainListener
) : ListAdapter<Category, CategoryMainAdapter.ViewHolder>(CategoryDiffCallback()) {

    interface CategoryMainListener {
        fun onMoreClick(item: Category)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position) ?: return
        holder.bind(item, listener)
    }

    class ViewHolder(val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Category, listener: CategoryMainListener) {
            val id = DATA.EMPTY + item.id
            val name = DATA.EMPTY + item.name
            val image = DATA.EMPTY + item.image

            binding.image.loadImage(false, image)
            binding.imageBlur.loadBlurImage(false, image, 50)

            if (name == DATA.EMPTY) {
                binding.name.visibility = View.GONE
            } else {
                binding.name.visibility = View.VISIBLE
                binding.name.text = name
            }

            binding.more.setOnClickListener { listener.onMoreClick(item) }
            binding.card.setOnClickListener {
                itemView.context.openActivity<CategoryTasksActivity>(
                    false, DATA.ID to id, DATA.NAME to name
                )
            }
        }
    }

    class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem == newItem
        }
    }
}
