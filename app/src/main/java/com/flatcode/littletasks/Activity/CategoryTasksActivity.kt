package com.flatcode.littletasks.Activity

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littletasks.Adapter.TaskAdapter
import com.flatcode.littletasks.Model.Task
import com.flatcode.littletasks.R
import com.flatcode.littletasks.Unit.CLASS
import com.flatcode.littletasks.Unit.DATA
import com.flatcode.littletasks.Unit.THEME
import com.flatcode.littletasks.Unit.VOID
import com.flatcode.littletasks.databinding.ActivityPageSwitchBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import java.text.MessageFormat

class CategoryTasksActivity : AppCompatActivity() {

    private var binding: ActivityPageSwitchBinding? = null
    private val context: Context = this@CategoryTasksActivity
    var list: ArrayList<Task?>? = null
    var adapter: TaskAdapter? = null
    var id: String? = null
    var name: String? = null
    var type: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        THEME.setThemeOfApp(context)
        super.onCreate(savedInstanceState)
        binding = ActivityPageSwitchBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        val intent = intent
        id = intent.getStringExtra(DATA.ID)
        name = intent.getStringExtra(DATA.NAME)

        type = DATA.TIMESTAMP
        binding!!.toolbar.nameSpace.text = name
        binding!!.toolbar.back.setOnClickListener { onBackPressed() }
        binding!!.toolbar.close.setOnClickListener { v: View? -> onBackPressed() }
        binding!!.add.add.setText(R.string.add_task)
        binding!!.add.item.setOnClickListener {
            VOID.IntentExtra(context, CLASS.TASK_ADD, DATA.CATEGORY_ID, id)
        }

        binding!!.toolbar.search.setOnClickListener {
            binding!!.toolbar.toolbar.visibility = View.GONE
            binding!!.toolbar.toolbarSearch.visibility = View.VISIBLE
            searchStatus = true
        }
        binding!!.toolbar.textSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                try {
                    adapter!!.filter.filter(s)
                } catch (e: Exception) {
                    //None
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })

        //binding.recyclerView.setHasFixedSize(true);
        list = ArrayList()
        adapter = TaskAdapter(context, list!!)
        binding!!.recyclerView.adapter = adapter
        binding!!.recyclerViewReverse.adapter = adapter

        binding!!.filter.all.setOnClickListener {
            type = DATA.TIMESTAMP
            getData(type, binding!!.recyclerView, binding!!.recyclerViewReverse)
        }
        binding!!.filter.points.setOnClickListener { UpToDown(binding!!.filter.a1, DATA.POINTS) }
        binding!!.filter.AVPoints.setOnClickListener {
            UpToDown(binding!!.filter.a2, DATA.AVAILABLE_POINTS)
        }
        binding!!.filter.add.setOnClickListener { UpToDown(binding!!.filter.a3, DATA.TIMESTAMP) }
        binding!!.filter.start.setOnClickListener { UpToDown(binding!!.filter.a4, DATA.START) }
        binding!!.filter.end.setOnClickListener { UpToDown(binding!!.filter.a5, DATA.END) }
    }

    private fun UpToDown(item: ImageView, Type: String?) {
        type = Type
        if (item.tag != null) {
            if (item.tag == "up") {
                getData(type, binding!!.recyclerView, binding!!.recyclerViewReverse)
                item.tag = "down"
                item.setImageResource(R.drawable.ic_down)
            } else if (item.tag == "down") {
                getData(type, binding!!.recyclerViewReverse, binding!!.recyclerView)
                item.tag = "up"
                item.setImageResource(R.drawable.ic_up)
            }
        } else {
            getData(type, binding!!.recyclerView, binding!!.recyclerViewReverse)
            item.tag = "down"
            item.setImageResource(R.drawable.ic_down)
        }
    }

    private fun getData(orderBy: String?, recyclerView: RecyclerView, recyclerView2: RecyclerView) {
        val reference: Query = FirebaseDatabase.getInstance().getReference(DATA.TASKS)
        reference.orderByChild(orderBy!!).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                list!!.clear()
                var i = 0
                for (snapshot in dataSnapshot.children) {
                    val item = snapshot.getValue(Task::class.java)!!
                    if (item.category == id) if (item.publisher == DATA.FirebaseUserUid) {
                        list!!.add(item)
                        i++
                    }
                }
                binding!!.toolbar.number.text = MessageFormat.format("( {0} )", i)
                recyclerView2.visibility = View.GONE
                binding!!.bar.visibility = View.GONE
                if (list!!.isNotEmpty()) {
                    recyclerView.visibility = View.VISIBLE
                    binding!!.emptyText.visibility = View.GONE
                } else {
                    recyclerView.visibility = View.GONE
                    binding!!.emptyText.visibility = View.VISIBLE
                }
                adapter!!.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private val points: Unit
        get() {
            val ref = FirebaseDatabase.getInstance().getReference(DATA.TASKS)
            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var i = 0
                    var a = 0
                    for (snapshot in dataSnapshot.children) {
                        val item = snapshot.getValue(Task::class.java)!!
                        if (item.category == id) {
                            i = i + item.points
                            a = a + item.aVPoints
                        }
                    }
                    binding!!.allPoints.text = MessageFormat.format("{0}{1}", DATA.EMPTY, i)
                    binding!!.av.text = MessageFormat.format("{0}{1}", DATA.EMPTY, a)
                    binding!!.level.text =
                        MessageFormat.format("{0}{1}", DATA.EMPTY, VOID.levelPoint(a, 10))
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }

    override fun onBackPressed() {
        if (searchStatus) {
            binding!!.toolbar.toolbar.visibility = View.VISIBLE
            binding!!.toolbar.toolbarSearch.visibility = View.GONE
            searchStatus = false
            binding!!.toolbar.textSearch.setText(DATA.EMPTY)
        } else super.onBackPressed()
    }

    override fun onResume() {
        super.onResume()
        getData(DATA.TIMESTAMP, binding!!.recyclerView, binding!!.recyclerViewReverse)
        points
    }

    override fun onRestart() {
        super.onRestart()
        getData(DATA.TIMESTAMP, binding!!.recyclerView, binding!!.recyclerViewReverse)
        points
    }

    companion object {
        var searchStatus = false
    }
}