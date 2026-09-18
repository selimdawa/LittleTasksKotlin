package com.flatcode.littletasks.ui.category

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littletasks.R
import com.flatcode.littletasks.databinding.FragmentCategoriesBinding
import com.flatcode.littletasks.model.Category
import com.flatcode.littletasks.ui.task.TaskAddActivity
import com.flatcode.littletasks.utils.DATA
import com.flatcode.littletasks.utils.dialogOptionDelete
import com.flatcode.littletasks.utils.openActivity
import com.flatcode.littletasks.utils.showMoreOptions
import com.flatcode.littletasks.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CategoriesFragment : Fragment(R.layout.fragment_categories),
    CategoryMainAdapter.CategoryMainListener {

    private val binding by viewBinding(FragmentCategoriesBinding::bind)

    private var adapter: CategoryMainAdapter? = null
    private val viewModel: CategoryViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CategoryMainAdapter(this)
        binding.recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.categories.collectLatest { newList ->
                    adapter?.submitList(newList)

                    binding.bar.visibility = View.GONE
                    if (newList.isNotEmpty()) {
                        binding.recyclerView.visibility = View.VISIBLE
                        binding.emptyText.visibility = View.GONE
                    } else {
                        binding.recyclerView.visibility = View.GONE
                        binding.emptyText.visibility = View.VISIBLE
                    }
                }
            }
        }

    }

    override fun onMoreClick(item: Category) {
        val options = arrayOf("Add Task", "Edit", "Delete")
        context?.showMoreOptions(options) { which ->
            when (which) {
                0 -> context?.openActivity<TaskAddActivity>(
                    false,
                    DATA.CATEGORY_ID to item.id,
                    DATA.PLAN_ID to item.plan
                )

                1 -> context?.openActivity<CategoryEditActivity>(
                    false,
                    DATA.CATEGORY_ID to item.id,
                    DATA.PLAN_ID to item.plan
                )

                2 -> context?.dialogOptionDelete(DATA.CATEGORIES) {
                    viewModel.deleteTask(DATA.CATEGORIES, item.id)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getCategories(DATA.NAME)
    }
}
