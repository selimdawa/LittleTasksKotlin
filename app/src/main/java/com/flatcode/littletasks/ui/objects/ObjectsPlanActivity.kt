package com.flatcode.littletasks.ui.objects

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
import com.flatcode.littletasks.databinding.ActivityObjectsBinding
import com.flatcode.littletasks.model.TaskItem
import com.flatcode.littletasks.utils.DATA
import com.flatcode.littletasks.utils.dialogOptionDelete
import com.flatcode.littletasks.utils.openActivity
import com.flatcode.littletasks.utils.showMoreOptions
import com.flatcode.littletasks.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ObjectsPlanActivity : AppCompatActivity(), ObjectAdapter.ObjectListener {

    private val binding by viewBinding(ActivityObjectsBinding::inflate)

    private val context: Context = this@ObjectsPlanActivity
    private var adapter: ObjectAdapter? = null
    private var id: String? = null
    private var name: String? = null
    private val viewModel: ObjectsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        id = intent.getStringExtra(DATA.ID)
        name = intent.getStringExtra(DATA.NAME)

        binding.toolbar.nameSpace.setText(R.string.objects_plan)
        binding.toolbar.back.setOnClickListener { handleBackPressed() }
        binding.toolbar.close.setOnClickListener { handleBackPressed() }
        binding.add.add.setOnClickListener {
            context.openActivity<ObjectsToPlanActivity>(false, DATA.ID to id)
        }

        binding.toolbar.search.setOnClickListener {
            binding.toolbar.root.getChildAt(0).visibility = View.GONE
            binding.toolbar.toolbarSearch.visibility = View.VISIBLE
            DATA.searchStatus = true
        }

        binding.toolbar.textSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter?.filter(s)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        adapter = ObjectAdapter(this)
        binding.recyclerView.adapter = adapter

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.objects.collectLatest { newList ->
                    adapter?.setFullList(newList)

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

    override fun onMoreClick(item: TaskItem) {
        val options = arrayOf("Edit", "Delete")
        context.showMoreOptions(options) { which ->
            when (which) {
                0 -> context.openActivity<ObjectEditActivity>(false, DATA.ID to item.id)
                1 -> context.dialogOptionDelete(DATA.OBJECTS) {
                    viewModel.deleteTask(DATA.OBJECTS, item.id)
                }
            }
        }
    }

    private fun handleBackPressed() {
        if (DATA.searchStatus) {
            binding.toolbar.root.getChildAt(0).visibility = View.VISIBLE
            binding.toolbar.toolbarSearch.visibility = View.GONE
            DATA.searchStatus = false
            binding.toolbar.textSearch.setText(DATA.EMPTY)
        } else {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        id?.let { viewModel.loadPlanObjects(it) }
    }
}
