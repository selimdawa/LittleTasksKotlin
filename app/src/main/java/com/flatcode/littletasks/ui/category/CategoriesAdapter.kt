package com.flatcode.littletasks.ui.category

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littletasks.databinding.ItemCategoriesBinding
import com.flatcode.littletasks.filter.CategoriesFilter
import com.flatcode.littletasks.model.Category
import com.flatcode.littletasks.utils.DATA
import com.flatcode.littletasks.utils.loadImage
import com.flatcode.littletasks.utils.openActivity
import java.text.MessageFormat

class CategoriesAdapter(
    private val listener: CategoryListener
) : ListAdapter<Category, CategoriesAdapter.ViewHolder>(CategoryDiffCallback()), Filterable {

    interface CategoryListener {
        fun onMoreClick(item: Category)
    }

    var fullList = ArrayList<Category?>()
    private var filter: CategoriesFilter? = null

    fun setFullList(newList: List<Category?>) {
        fullList = ArrayList(newList)
        submitList(newList.filterNotNull())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemCategoriesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position) ?: return
        holder.bind(item, listener)
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = CategoriesFilter(fullList, this)
        }
        return filter!!
    }

    class ViewHolder(private val binding: ItemCategoriesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Category, listener: CategoryListener) {
            val id = DATA.EMPTY + item.id
            val name = DATA.EMPTY + item.name
            val image = DATA.EMPTY + item.image
            val taskCount = item.taskCount

            binding.image.loadImage(false, image)

            if (name == DATA.EMPTY) {
                binding.name.visibility = View.GONE
            } else {
                binding.name.visibility = View.VISIBLE
                binding.name.text = name
            }

            binding.number.text = MessageFormat.format("{0}{1}", DATA.EMPTY, taskCount)
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