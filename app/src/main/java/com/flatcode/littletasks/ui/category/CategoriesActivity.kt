package com.flatcode.littletasks.ui.category

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littletasks.R
import com.flatcode.littletasks.databinding.ActivityPageStaggeredBinding
import com.flatcode.littletasks.model.Category
import com.flatcode.littletasks.ui.plan.PlansActivity
import com.flatcode.littletasks.ui.task.TaskAddActivity
import com.flatcode.littletasks.utils.DATA
import com.flatcode.littletasks.utils.dialogOptionDelete
import com.flatcode.littletasks.utils.openActivity
import com.flatcode.littletasks.utils.showMoreOptions
import com.flatcode.littletasks.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.MessageFormat

@AndroidEntryPoint
class CategoriesActivity : AppCompatActivity(), CategoriesAdapter.CategoryListener {

    private val binding by viewBinding(ActivityPageStaggeredBinding::inflate)

    private val context: Context = this@CategoriesActivity
    private val list = ArrayList<Category?>()
    private var adapter: CategoriesAdapter? = null
    private val viewModel: CategoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding.toolbar.nameSpace.setText(R.string.categories)
        binding.toolbar.back.setOnClickListener { handleBackPressed() }
        binding.toolbar.close.setOnClickListener { handleBackPressed() }
        binding.add.add.setText(R.string.add_category)
        binding.add.add.setOnClickListener {
            context.openActivity(PlansActivity::class.java, DATA.NEW_PLAN to "true")
        }

        binding.toolbar.search.setOnClickListener {
            binding.toolbar.toolbar.visibility = View.GONE
            binding.toolbar.toolbarSearch.visibility = View.VISIBLE
            DATA.searchStatus = true
        }

        binding.toolbar.textSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter?.filter?.filter(s)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        adapter = CategoriesAdapter(context, this)
        binding.recyclerView.adapter = adapter

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.categories.collectLatest { newList ->
                    list.clear()
                    list.addAll(newList)
                    adapter?.setFullList(newList)

                    binding.toolbar.number.text = MessageFormat.format("( {0} )", list.size)
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
    }

    override fun onMoreClick(item: Category) {
        val options = arrayOf("Add Task", "Edit", "Delete")
        context.showMoreOptions(options) { which ->
            when (which) {
                0 -> context.openActivity(
                    TaskAddActivity::class.java,
                    DATA.CATEGORY_ID to item.id,
                    DATA.PLAN_ID to item.plan
                )

                1 -> context.openActivity(
                    CategoryEditActivity::class.java,
                    DATA.CATEGORY_ID to item.id,
                    DATA.PLAN_ID to item.plan
                )

                2 -> context.dialogOptionDelete(DATA.CATEGORIES, item.id, item.name ?: "") {
                    viewModel.deleteTask(DATA.CATEGORIES, item.id ?: "")
                }
            }
        }
    }

    private fun handleBackPressed() {
        if (DATA.searchStatus) {
            binding.toolbar.toolbar.visibility = View.VISIBLE
            binding.toolbar.toolbarSearch.visibility = View.GONE
            DATA.searchStatus = false
            binding.toolbar.textSearch.setText(DATA.EMPTY)
        } else {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getCategories(DATA.NAME)
    }
}
