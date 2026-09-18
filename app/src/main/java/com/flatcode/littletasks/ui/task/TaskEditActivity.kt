package com.flatcode.littletasks.ui.task

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littletasks.R
import com.flatcode.littletasks.databinding.ActivityTaskAddBinding
import com.flatcode.littletasks.databinding.LayoutLoadingDialogBinding
import com.flatcode.littletasks.utils.DATA
import com.flatcode.littletasks.utils.loadImage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TaskEditActivity : AppCompatActivity() {

    private var _binding: ActivityTaskAddBinding? = null
    private val binding get() = _binding!!

    private val context: Context = this@TaskEditActivity
    private var progressDialog: AlertDialog? = null
    private var taskId: String? = null
    private var categoryId: String? = null
    private val viewModel: TaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        _binding = ActivityTaskAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        taskId = intent.getStringExtra(DATA.TASK_ID)
        categoryId = intent.getStringExtra(DATA.CATEGORY_ID)

        binding.toolbar.nameSpace.setText(R.string.edit_task)
        binding.toolbar.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.nameLabel.setText(R.string.task_name)
        binding.toolbar.ok.setOnClickListener { validateData() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.taskInfo.collectLatest { task ->
                    task?.let {
                        binding.nameEt.setText(it.name)
                        binding.PointsEt.setText(it.points.toString())
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.categoryInfo.collectLatest { category ->
                    category?.let {
                        binding.category.text = it.name
                        binding.image.loadImage(false, it.image)
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.actionResult.collectLatest { result ->
                    result?.let {
                        dismissLoading()
                        it.onSuccess {
                            Toast.makeText(context, "Task info updated...", Toast.LENGTH_SHORT)
                                .show()
                            finish()
                        }.onFailure { e ->
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }
        }

        taskId?.let { viewModel.loadTaskInfo(it) }
        categoryId?.let { viewModel.loadCategoryInfo(it) }
    }

    private fun showLoading() {
        if (progressDialog == null) {
            val loadingBinding = LayoutLoadingDialogBinding.inflate(layoutInflater)
            progressDialog =
                AlertDialog.Builder(context).setView(loadingBinding.root).setCancelable(false)
                    .create()
        }
        progressDialog?.show()
    }

    private fun dismissLoading() {
        progressDialog?.dismiss()
    }

    private fun validateData() {
        val name = binding.nameEt.text.toString().trim()
        val pointsStr = binding.PointsEt.text.toString().trim()

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(context, "Enter Name...", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(pointsStr)) {
            Toast.makeText(context, "Enter Points...", Toast.LENGTH_SHORT).show()
        } else {
            showLoading()
            val points = pointsStr.toIntOrNull() ?: 0
            viewModel.updateTask(taskId ?: "", name, points)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissLoading()
        _binding = null
    }
}
