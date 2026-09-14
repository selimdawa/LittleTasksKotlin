package com.flatcode.littletasks.ui.task

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littletasks.R
import com.flatcode.littletasks.core.utils.DATA
import com.flatcode.littletasks.core.utils.VOID
import com.flatcode.littletasks.databinding.ActivityTaskAddBinding
import dagger.hilt.android.AndroidEntryPoint

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
        super.onCreate(savedInstanceState)
        _binding = ActivityTaskAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        taskId = intent.getStringExtra(DATA.TASK_ID)
        categoryId = intent.getStringExtra(DATA.CATEGORY_ID)

        binding.toolbar.nameSpace.setText(R.string.edit_task)
        binding.toolbar.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.name.setText(R.string.task_name)
        binding.toolbar.ok.setOnClickListener { validateData() }

        viewModel.taskInfo.observe(this) { task ->
            binding.nameEt.setText(task.name)
            binding.PointsEt.setText(task.points.toString())
        }

        viewModel.categoryInfo.observe(this) { category ->
            binding.category.text = category.name
            VOID.GlideImage(false, context, category.image, binding.image)
        }

        viewModel.actionResult.observe(this) { result ->
            dismissLoading()
            result.onSuccess {
                Toast.makeText(context, "Task info updated...", Toast.LENGTH_SHORT).show()
                finish()
            }.onFailure { e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        taskId?.let { viewModel.loadTaskInfo(it) }
        categoryId?.let { viewModel.loadCategoryInfo(it) }
    }

    private fun showLoading() {
        if (progressDialog == null) {
            progressDialog = AlertDialog.Builder(context)
                .setView(R.layout.layout_loading_dialog)
                .setCancelable(false)
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
