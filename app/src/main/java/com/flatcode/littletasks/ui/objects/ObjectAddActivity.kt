package com.flatcode.littletasks.ui.objects

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littletasks.R
import com.flatcode.littletasks.databinding.ActivityObjectAddBinding
import com.flatcode.littletasks.model.TaskItem
import com.flatcode.littletasks.utils.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ObjectAddActivity : BaseActivity(), ObjectAddAdapter.ObjectAddListener {

    private var _binding: ActivityObjectAddBinding? = null
    private val binding get() = _binding!!

    private val context: Context = this@ObjectAddActivity
    private val list = ArrayList<TaskItem>()
    private var adapter: ObjectAddAdapter? = null
    private val editorsChoice = TaskItem()
    private val viewModel: ObjectsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityObjectAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.nameSpace.setText(R.string.add_new_object)
        binding.toolbar.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        adapter = ObjectAddAdapter(this, list)
        binding.recyclerView.adapter = adapter

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.actionResult.collectLatest { result ->
                    result?.onSuccess {
                        Toast.makeText(context, "Successfully uploaded...", Toast.LENGTH_SHORT)
                            .show()
                        // Note: The adapter needs to know which item was uploaded to show 'Done'
                        // This is tricky with the current multi-item 'repeat(20)' logic.
                        // Ideally, we'd have a more robust way to track individual upload states.
                    }?.onFailure { e ->
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        ideaPosts()
    }

    override fun onAddClick(name: String, points: String, add: View, ok: View) {
        if (name.isEmpty()) {
            Toast.makeText(context, "Enter Name...", Toast.LENGTH_SHORT).show()
        } else if (points.isEmpty()) {
            Toast.makeText(context, "Enter Points...", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.addObject(name, points.toIntOrNull() ?: 0)
            // UI update for individual item (immediate feedback)
            add.visibility = View.GONE
            ok.visibility = View.VISIBLE
        }
    }

    private fun ideaPosts() {
        val previousSize = list.size
        if (previousSize > 0) {
            list.clear()
            adapter?.notifyItemRangeRemoved(0, previousSize)
        }

        repeat(20) {
            list.add(editorsChoice)
        }
        adapter?.notifyItemRangeInserted(0, list.size)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
