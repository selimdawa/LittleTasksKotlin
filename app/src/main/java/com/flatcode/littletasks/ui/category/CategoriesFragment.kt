package com.flatcode.littletasks.ui.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.flatcode.littletasks.model.Category
import com.flatcode.littletasks.databinding.FragmentCategoriesBinding
import com.flatcode.littletasks.ui.task.TaskAddActivity
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littletasks.utils.DATA
import com.flatcode.littletasks.utils.dialogOptionDelete
import com.flatcode.littletasks.utils.openActivity
import com.flatcode.littletasks.utils.showMoreOptions
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CategoriesFragment : Fragment(), CategoryMainAdapter.CategoryMainListener {

    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!

    private val list = ArrayList<Category?>()
    private var adapter: CategoryMainAdapter? = null
    private val viewModel: CategoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)

        adapter = CategoryMainAdapter(context, list, this)
        binding.recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.categories.collectLatest { newList ->
                    list.clear()
                    list.addAll(newList)
                    adapter?.notifyDataSetChanged()

                    binding.bar.visibility = View.GONE
                    if (list.isNotEmpty()) {
                        binding.recyclerView.visibility = View.VISIBLE
                        binding.emptyText.visibility = View.GONE
                    } else {
                        binding.recyclerView.visibility = View.GONE
                        binding.emptyText.visibility = View.VISIBLE
                    }
                }
            }
        }

        return binding.root
    }

    override fun onMoreClick(item: Category) {
        val options = arrayOf("Add Task", "Edit", "Delete")
        context?.showMoreOptions(options) { which ->
            when (which) {
                0 -> context?.openActivity(TaskAddActivity::class.java, DATA.CATEGORY_ID to item.id, DATA.PLAN_ID to item.plan)
                1 -> context?.openActivity(CategoryEditActivity::class.java, DATA.CATEGORY_ID to item.id, DATA.PLAN_ID to item.plan)
                2 -> context?.dialogOptionDelete(DATA.CATEGORIES, item.id, item.name ?: "") {
                    viewModel.deleteTask(DATA.CATEGORIES, item.id ?: "")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getCategories(DATA.NAME)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
