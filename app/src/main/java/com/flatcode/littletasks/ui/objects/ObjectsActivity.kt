package com.flatcode.littletasks.ui.objects

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littletasks.R
import com.flatcode.littletasks.core.utils.*
import com.flatcode.littletasks.data.model.TaskItem
import com.flatcode.littletasks.databinding.ActivityObjectsBinding
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ObjectsActivity : AppCompatActivity(), ObjectAdapter.ObjectListener {

    private var _binding: ActivityObjectsBinding? = null
    private val binding get() = _binding!!

    private val context: Context = this@ObjectsActivity
    private val list = ArrayList<TaskItem?>()
    private var adapter: ObjectAdapter? = null
    private val viewModel: ObjectsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityObjectsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.nameSpace.setText(R.string.objects)
        binding.toolbar.back.setOnClickListener { handleBackPressed() }
        binding.toolbar.close.setOnClickListener { handleBackPressed() }
        binding.add.add.setText(R.string.add_object)
        binding.add.item.setOnClickListener { context.openActivity(ObjectAddActivity::class.java) }

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

        adapter = ObjectAdapter(context, list, this)
        binding.recyclerView.adapter = adapter

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.objects.collectLatest { newList ->
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
    }

    override fun onMoreClick(item: TaskItem) {
        val options = arrayOf("Edit", "Delete")
        context.showMoreOptions(options) { which ->
            when (which) {
                0 -> context.openActivity(ObjectEditActivity::class.java, DATA.ID to item.id)
                1 -> context.dialogOptionDelete(DATA.OBJECTS, item.id, item.name ?: "") {
                    viewModel.deleteTask(DATA.OBJECTS, item.id ?: "")
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
        viewModel.loadAllObjects()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
