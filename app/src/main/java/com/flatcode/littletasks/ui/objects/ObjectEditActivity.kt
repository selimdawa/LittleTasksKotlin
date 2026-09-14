package com.flatcode.littletasks.ui.objects

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littletasks.R
import com.flatcode.littletasks.core.utils.DATA
import com.flatcode.littletasks.databinding.ActivityObjectEditBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ObjectEditActivity : AppCompatActivity() {

    private var _binding: ActivityObjectEditBinding? = null
    private val binding get() = _binding!!

    private val context: Context = this@ObjectEditActivity
    private var id: String? = null
    private var progressDialog: AlertDialog? = null
    private val viewModel: ObjectsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityObjectEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        id = intent.getStringExtra(DATA.ID)

        binding.toolbar.nameSpace.setText(R.string.edit_object)
        binding.toolbar.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.name.setText(R.string.object_name)
        binding.go.setOnClickListener { validateData() }

        viewModel.objectInfo.observe(this) { item ->
            binding.nameEt.setText(item.name)
            binding.PointsEt.setText(item.points.toString())
        }

        viewModel.actionResult.observe(this) { result ->
            dismissLoading()
            result.onSuccess {
                Toast.makeText(context, "Object updated...", Toast.LENGTH_SHORT).show()
                finish()
            }.onFailure { e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        id?.let { viewModel.loadObjectInfo(it) }
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
            Toast.makeText(context, "Enter name...", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(pointsStr)) {
            Toast.makeText(context, "Enter points...", Toast.LENGTH_SHORT).show()
        } else {
            showLoading()
            val points = pointsStr.toIntOrNull() ?: 0
            viewModel.updateObject(id ?: "", name, points)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissLoading()
        _binding = null
    }
}
