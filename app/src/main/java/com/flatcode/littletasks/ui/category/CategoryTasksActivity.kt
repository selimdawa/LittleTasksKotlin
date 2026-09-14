package com.flatcode.littletasks.ui.category

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littletasks.R
import com.flatcode.littletasks.core.utils.CLASS
import com.flatcode.littletasks.core.utils.DATA
import com.flatcode.littletasks.core.utils.VOID
import com.flatcode.littletasks.data.model.Task
import com.flatcode.littletasks.databinding.ActivityPageSwitchBinding
import com.flatcode.littletasks.ui.task.TaskAdapter
import dagger.hilt.android.AndroidEntryPoint
import java.text.MessageFormat

@AndroidEntryPoint
class CategoryTasksActivity : AppCompatActivity() {

    private var _binding: ActivityPageSwitchBinding? = null
    private val binding get() = _binding!!

    private val context: Context = this@CategoryTasksActivity
    private val list = ArrayList<Task?>()
    private var adapter: TaskAdapter? = null
    private var id: String? = null
    private var name: String? = null
    private var currentSortType = DATA.TIMESTAMP
    private val viewModel: CategoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityPageSwitchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        id = intent.getStringExtra(DATA.ID)
        name = intent.getStringExtra(DATA.NAME)

        currentSortType = DATA.TIMESTAMP
        binding.toolbar.nameSpace.text = name
        binding.toolbar.back.setOnClickListener { handleBackPressed() }
        binding.toolbar.close.setOnClickListener { handleBackPressed() }
        binding.add.add.setText(R.string.add_task)
        binding.add.item.setOnClickListener {
            VOID.IntentExtra(context, CLASS.TASK_ADD, DATA.CATEGORY_ID, id)
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

        adapter = TaskAdapter(context, list)
        binding.recyclerView.adapter = adapter
        binding.recyclerViewReverse.adapter = adapter

        binding.filter.all.setOnClickListener {
            currentSortType = DATA.TIMESTAMP
            viewModel.getCategoryTasks(id ?: "", currentSortType)
            binding.recyclerView.visibility = View.VISIBLE
            binding.recyclerViewReverse.visibility = View.GONE
        }
        binding.filter.points.setOnClickListener { toggleSortDirection(binding.filter.a1, DATA.POINTS) }
        binding.filter.AVPoints.setOnClickListener { toggleSortDirection(binding.filter.a2, DATA.AVAILABLE_POINTS) }
        binding.filter.add.setOnClickListener { toggleSortDirection(binding.filter.a3, DATA.TIMESTAMP) }
        binding.filter.start.setOnClickListener { toggleSortDirection(binding.filter.a4, DATA.START) }
        binding.filter.end.setOnClickListener { toggleSortDirection(binding.filter.a5, DATA.END) }

        viewModel.categoryTasks.observe(this) { newList ->
            list.clear()
            list.addAll(newList)
            adapter?.notifyDataSetChanged()

            binding.toolbar.number.text = MessageFormat.format("( {0} )", list.size)
            binding.bar.visibility = View.GONE
            if (list.isNotEmpty()) {
                binding.emptyText.visibility = View.GONE
            } else {
                binding.recyclerView.visibility = View.GONE
                binding.recyclerViewReverse.visibility = View.GONE
                binding.emptyText.visibility = View.VISIBLE
            }
        }

        viewModel.pointsSummary.observe(this) { (all, av, level) ->
            binding.allPoints.text = all.toString()
            binding.av.text = av.toString()
            binding.level.text = level.toString()
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        var searchStatus = false
    }
}
