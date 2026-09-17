package com.flatcode.littletasks.ui.objects

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littletasks.R
import com.flatcode.littletasks.core.utils.*
import com.flatcode.littletasks.model.TaskItem
import com.flatcode.littletasks.databinding.ActivityObjectsBinding
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littletasks.utils.DATA
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ObjectsToPlanActivity : AppCompatActivity(), ObjectOptionAdapter.ObjectOptionListener {

    private var _binding: ActivityObjectsBinding? = null
    private val binding get() = _binding!!

    private val context: Context = this@ObjectsToPlanActivity
    private val list = ArrayList<TaskItem?>()
    private var adapter: ObjectOptionAdapter? = null
    private var id: String? = null
    private val viewModel: ObjectsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        _binding = ActivityObjectsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        id = intent.getStringExtra(DATA.ID)

        binding.toolbar.nameSpace.setText(R.string.add_object_to_plan)
        binding.toolbar.back.setOnClickListener { handleBackPressed() }
        binding.toolbar.close.setOnClickListener { handleBackPressed() }
        binding.add.item.visibility = View.GONE

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

        adapter = ObjectOptionAdapter(context, list, id, this)
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

    override fun onOptionClick(item: TaskItem) {
        lifecycleScope.launch {
            val isAdded = viewModel.observePlanStatus(item.id!!, id!!).first()
            viewModel.togglePlan(item.id!!, id!!, !isAdded)
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
