package com.flatcode.littletasks.Activity

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littletasks.Adapter.ObjectAddAdapter
import com.flatcode.littletasks.Model.OBJECT
import com.flatcode.littletasks.R
import com.flatcode.littletasks.Unit.THEME
import com.flatcode.littletasks.databinding.ActivityObjectAddBinding

class ObjectAddActivity : AppCompatActivity() {

    private var binding: ActivityObjectAddBinding? = null
    var context: Context = this@ObjectAddActivity
    var list: ArrayList<OBJECT>? = null
    var adapter: ObjectAddAdapter? = null
    var editorsChoice = OBJECT()

    override fun onCreate(savedInstanceState: Bundle?) {
        THEME.setThemeOfApp(context)
        super.onCreate(savedInstanceState)
        binding = ActivityObjectAddBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)
        binding!!.toolbar.nameSpace.setText(R.string.add_new_object)
        binding!!.toolbar.back.setOnClickListener { v: View? -> onBackPressed() }

        //binding.recyclerView.setHasFixedSize(true);
        list = ArrayList()
        adapter = ObjectAddAdapter(context, list!!)
        binding!!.recyclerView.adapter = adapter

        IdeaPosts()
    }

    private fun IdeaPosts() {
        list!!.clear()
        for (i in 0..19) {
            list!!.add(editorsChoice)
        }
        adapter!!.notifyDataSetChanged()
    }
}