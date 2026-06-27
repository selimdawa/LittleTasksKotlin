package com.flatcode.littletasks.Activity

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littletasks.Adapter.ObjectAddAdapter
import com.flatcode.littletasks.Model.TaskItem
import com.flatcode.littletasks.R
import com.flatcode.littletasks.Unit.THEME
import com.flatcode.littletasks.databinding.ActivityObjectAddBinding

class ObjectAddActivity : AppCompatActivity() {

    private var _binding: ActivityObjectAddBinding? = null
    private val binding get() = _binding!!

    private val context: Context = this@ObjectAddActivity
    private val list = ArrayList<TaskItem>()
    private var adapter: ObjectAddAdapter? = null
    private val editorsChoice = TaskItem()

    override fun onCreate(savedInstanceState: Bundle?) {
        THEME.setThemeOfApp(context)
        super.onCreate(savedInstanceState)
        _binding = ActivityObjectAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.nameSpace.setText(R.string.add_new_object)
        binding.toolbar.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        adapter = ObjectAddAdapter(context, list)
        binding.recyclerView.adapter = adapter

        ideaPosts()
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