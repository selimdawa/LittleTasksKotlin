package com.flatcode.littletasks.ui.objects

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
import com.flatcode.littletasks.databinding.ActivityObjectsBinding
import com.flatcode.littletasks.model.TaskItem
import com.flatcode.littletasks.utils.DATA
import com.flatcode.littletasks.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ObjectsToPlanActivity : AppCompatActivity(), ObjectOptionAdapter.ObjectOptionListener {

    private val binding by viewBinding(ActivityObjectsBinding::inflate)

    private var adapter: ObjectOptionAdapter? = null
    private var id: String? = null
    private val viewModel: ObjectsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        id = intent.getStringExtra(DATA.ID)

        binding.toolbar.nameSpace.setText(R.string.add_object_to_plan)
        binding.toolbar.back.setOnClickListener { handleBackPressed() }
        binding.toolbar.close.setOnClickListener { handleBackPressed() }
        binding.add.add.visibility = View.GONE

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

        adapter = ObjectOptionAdapter(id, this)
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

    override fun onOptionClick(item: TaskItem) {
        lifecycleScope.launch {
            val isAdded = viewModel.observePlanStatus(item.id, id!!).first()
            viewModel.togglePlan(item.id, id!!, !isAdded)
        }
    }

    override fun isPlan(objectId: String, planId: String, imageView: ImageView) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.observePlanStatus(objectId, planId).collectLatest { isAdded ->
                    if (isAdded) {
                        imageView.setImageResource(R.drawable.ic_heart_selected)
                    } else {
                        imageView.setImageResource(R.drawable.ic_heart_unselected)
                    }
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
        viewModel.loadAllObjects()
    }
}
