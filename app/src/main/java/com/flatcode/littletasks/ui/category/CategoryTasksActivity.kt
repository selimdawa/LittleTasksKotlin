package com.flatcode.littletasks.ui.category

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littletasks.R
import com.flatcode.littletasks.databinding.ActivityPageSwitchBinding
import com.flatcode.littletasks.model.Task
import com.flatcode.littletasks.ui.task.TaskAdapter
import com.flatcode.littletasks.ui.task.TaskAddActivity
import com.flatcode.littletasks.ui.task.TaskEditActivity
import com.flatcode.littletasks.utils.DATA
import com.flatcode.littletasks.utils.dialogOptionDelete
import com.flatcode.littletasks.utils.openActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.MessageFormat

import com.flatcode.littletasks.utils.viewBinding

@AndroidEntryPoint
class CategoryTasksActivity : AppCompatActivity(), TaskAdapter.TaskListener {

    private val binding by viewBinding(ActivityPageSwitchBinding::inflate)

    private val context: Context = this@CategoryTasksActivity
    private var adapter: TaskAdapter? = null
    private var id: String? = null
    private var name: String? = null
    private var currentSortType = DATA.TIMESTAMP
    private val viewModel: CategoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        id = intent.getStringExtra(DATA.ID)
        name = intent.getStringExtra(DATA.NAME)

        currentSortType = DATA.TIMESTAMP
        binding.toolbar.nameSpace.text = name
        binding.toolbar.back.setOnClickListener { handleBackPressed() }
        binding.toolbar.close.setOnClickListener { handleBackPressed() }
        binding.add.add.setText(R.string.add_task)
        binding.add.add.setOnClickListener {
            context.openActivity<TaskAddActivity>(false, DATA.CATEGORY_ID to id)
        }

        binding.toolbar.search.setOnClickListener {
            binding.toolbar.toolbar.visibility = View.GONE
            binding.toolbar.toolbarSearch.visibility = View.VISIBLE
            searchStatus = true
        }

        binding.toolbar.textSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter?.filter?.filter(s)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        adapter = TaskAdapter(this)
        binding.recyclerView.adapter = adapter
        binding.recyclerViewReverse.adapter = adapter

        binding.filter.all.setOnClickListener {
            currentSortType = DATA.TIMESTAMP
            viewModel.getCategoryTasks(id ?: "", currentSortType)
            binding.recyclerView.visibility = View.VISIBLE
            binding.recyclerViewReverse.visibility = View.GONE
        }
        binding.filter.points.setOnClickListener {
            toggleSortDirection(
                binding.filter.a1, DATA.POINTS
            )
        }
        binding.filter.AVPoints.setOnClickListener {
            toggleSortDirection(
                binding.filter.a2, DATA.AVAILABLE_POINTS
            )
        }
        binding.filter.add.setOnClickListener {
            toggleSortDirection(
                binding.filter.a3, DATA.TIMESTAMP
            )
        }
        binding.filter.start.setOnClickListener {
            toggleSortDirection(
                binding.filter.a4, DATA.START
            )
        }
        binding.filter.end.setOnClickListener { toggleSortDirection(binding.filter.a5, DATA.END) }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.categoryTasks.collectLatest { newList ->
                    adapter?.setFullList(newList)

                    binding.toolbar.number.text = MessageFormat.format("( {0} )", newList.size)
                    binding.bar.visibility = View.GONE
                    if (newList.isNotEmpty()) {
                        binding.recyclerView.visibility = View.VISIBLE
                        binding.recyclerViewReverse.visibility = View.GONE
                        binding.emptyText.visibility = View.GONE
                    } else {
                        binding.recyclerView.visibility = View.GONE
                        binding.recyclerViewReverse.visibility = View.GONE
                        binding.emptyText.visibility = View.VISIBLE
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.pointsSummary.collectLatest { (all, av, level) ->
                    binding.allPoints.text = all.toString()
                    binding.av.text = av.toString()
                    binding.level.text = level.toString()
                }
            }
        }
    }

    override fun onMoreClick(item: Task) {
        val options = when {
            item.start == 0L && item.end == 0L -> arrayOf("Edit", "Delete")
            item.start != 0L && item.end == 0L -> arrayOf("Edit", "Delete", "Start Again")
            item.start != 0L -> arrayOf("Edit", "Delete", "Start Again", "Not End")
            else -> arrayOf()
        }

        MaterialAlertDialogBuilder(context).setTitle("Choose Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> context.openActivity<TaskEditActivity>(
                        false,
                        DATA.TASK_ID to item.id,
                        DATA.CATEGORY_ID to item.category
                    )

                    1 -> context.dialogOptionDelete(DATA.TASKS) {
                        viewModel.deleteTask(DATA.TASKS, item.id)
                    }

                    2 -> viewModel.updateTaskStatus(
                        item.id, startStatus = true, endStatus = false
                    )

                    3 -> viewModel.updateTaskStatus(
                        item.id, startStatus = false, endStatus = true
                    )
                }
            }.show()
    }

    override fun onFavoriteClick(item: Task) {
        viewModel.toggleFavorite(item)
    }

    override fun onTaskClick(item: Task) {
        viewModel.onTaskAction(item)
    }

    override fun isFavorite(taskId: String, userId: String, imageView: ImageView) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.observeFavoriteStatus(taskId, userId).collectLatest { isFav ->
                    if (isFav) {
                        imageView.setImageResource(R.drawable.ic_remove)
                    } else {
                        imageView.setImageResource(R.drawable.ic___add)
                    }
                }
            }
        }
    }

    private fun toggleSortDirection(imageView: ImageView, targetSortType: String) {
        currentSortType = targetSortType
        if (imageView.tag == "up") {
            viewModel.getCategoryTasks(id ?: "", currentSortType)
            binding.recyclerView.visibility = View.VISIBLE
            binding.recyclerViewReverse.visibility = View.GONE
            imageView.tag = "down"
            imageView.setImageResource(R.drawable.ic_down)
        } else {
            viewModel.getCategoryTasks(id ?: "", currentSortType)
            binding.recyclerView.visibility = View.GONE
            binding.recyclerViewReverse.visibility = View.VISIBLE
            imageView.tag = "up"
            imageView.setImageResource(R.drawable.ic_up)
        }
    }

    private fun handleBackPressed() {
        if (searchStatus) {
            binding.toolbar.toolbar.visibility = View.VISIBLE
            binding.toolbar.toolbarSearch.visibility = View.GONE
            searchStatus = false
            binding.toolbar.textSearch.setText(DATA.EMPTY)
        } else {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getCategoryTasks(id ?: "", currentSortType)
    }

    companion object {
        var searchStatus = false
    }
}
