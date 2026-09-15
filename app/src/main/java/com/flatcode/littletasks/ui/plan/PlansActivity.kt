package com.flatcode.littletasks.ui.plan

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littletasks.R
import com.flatcode.littletasks.core.utils.DATA
import com.flatcode.littletasks.core.utils.openActivity
import com.flatcode.littletasks.data.model.Plan
import com.flatcode.littletasks.databinding.ActivityPlansBinding
import dagger.hilt.android.AndroidEntryPoint
import java.text.MessageFormat

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PlansActivity : AppCompatActivity() {

    private var _binding: ActivityPlansBinding? = null
    private val binding get() = _binding!!

    private val context: Context = this@PlansActivity
    private val list = ArrayList<Plan?>()
    private var adapter: PlanAdapter? = null
    private var isNew = true
    private val viewModel: PlanViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityPlansBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val newPlan = intent.getStringExtra(DATA.NEW_PLAN)
        isNew = newPlan == "true"

        binding.toolbar.nameSpace.setText(R.string.plans)
        binding.toolbar.back.setOnClickListener { handleBackPressed() }
        binding.toolbar.close.setOnClickListener { handleBackPressed() }
        binding.add.add.setText(R.string.add_plan)
        binding.add.item.setOnClickListener { context.openActivity(PlanAddActivity::class.java) }

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

        adapter = PlanAdapter(context, list, isNew)
        binding.recyclerView.adapter = adapter

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.plans.collectLatest { newList ->
                    list.clear()
                    list.addAll(newList)
                    adapter?.notifyDataSetChanged()

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
        viewModel.loadPlans()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
