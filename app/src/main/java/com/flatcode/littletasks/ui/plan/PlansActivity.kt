package com.flatcode.littletasks.ui.plan

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
import com.flatcode.littletasks.databinding.ActivityPlansBinding
import com.flatcode.littletasks.model.Plan
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
class PlansActivity : AppCompatActivity(), PlanAdapter.PlanListener {

    private val binding by viewBinding(ActivityPlansBinding::inflate)

    private val context: Context = this@PlansActivity
    private var adapter: PlanAdapter? = null
    private var isNew = true
    private val viewModel: PlanViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val newPlan = intent.getStringExtra(DATA.NEW_PLAN)
        isNew = newPlan == "true"

        binding.toolbar.nameSpace.setText(R.string.plans)
        binding.toolbar.back.setOnClickListener { handleBackPressed() }
        binding.toolbar.close.setOnClickListener { handleBackPressed() }
        binding.add.add.setText(R.string.add_plan)
        binding.add.add.setOnClickListener { context.openActivity<PlanAddActivity>() }

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

        adapter = PlanAdapter(isNew, this)
        binding.recyclerView.adapter = adapter

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.plans.collectLatest { newList ->
                    adapter?.setFullList(newList)

                    binding.toolbar.number.text = MessageFormat.format("( {0} )", newList.size)
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

    override fun onMoreClick(item: Plan) {
        val options = arrayOf("Edit", "Delete")
        context.showMoreOptions(options) { which ->
            when (which) {
                0 -> context.openActivity<PlanEditActivity>(false, DATA.ID to item.id)
                1 -> context.dialogOptionDelete(DATA.PLANS) {
                    viewModel.deletePlan(item.id)
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
        viewModel.loadPlans()
    }
}
