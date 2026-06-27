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
import com.google.firebase.database.ValueEventListener
import java.text.MessageFormat

class CategoryTasksActivity : AppCompatActivity() {

    private var _binding: ActivityPageSwitchBinding? = null
    private val binding get() = _binding!!

    private val context: Context = this@CategoryTasksActivity
    private val list = ArrayList<Task?>()
    private var adapter: TaskAdapter? = null
    private var id: String? = null
    private var name: String? = null
    private var currentSortType = DATA.TIMESTAMP

    override fun onCreate(savedInstanceState: Bundle?) {
        THEME.setThemeOfApp(context)
        super.onCreate(savedInstanceState)
        _binding = ActivityPageSwitchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        id = intent.getStringExtra(DATA.ID)
        name = intent.getStringExtra(DATA.NAME)

        currentSortType = DATA.TIMESTAMP
        binding.toolbar.nameSpace.text = name
        binding.toolbar.back.setOnClickListener { handleBackPressed() }
        binding.toolbar.close.setOnClickListener { handleBackPressed() }
        binding.add.add.setText(R.string.add_task)
        binding.add.item.setOnClickListener {
            VOID.IntentExtra(context, CLASS.TASK_ADD, DATA.CATEGORY_ID, id)
        }

        binding.toolbar.search.setOnClickListener {
            binding.toolbar.toolbar.visibility = View.GONE
            binding.toolbar.toolbarSearch.visibility = View.VISIBLE
            searchStatus = true
        }

        binding.toolbar.textSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter?.filter?.filter(s)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        adapter = TaskAdapter(context, list)
        binding.recyclerView.adapter = adapter
        binding.recyclerViewReverse.adapter = adapter

        binding.filter.all.setOnClickListener {
            currentSortType = DATA.TIMESTAMP
            fetchData(currentSortType, binding.recyclerView, binding.recyclerViewReverse)
        }
        binding.filter.points.setOnClickListener { toggleSortDirection(binding.filter.a1, DATA.POINTS) }
        binding.filter.AVPoints.setOnClickListener { toggleSortDirection(binding.filter.a2, DATA.AVAILABLE_POINTS) }
        binding.filter.add.setOnClickListener { toggleSortDirection(binding.filter.a3, DATA.TIMESTAMP) }
        binding.filter.start.setOnClickListener { toggleSortDirection(binding.filter.a4, DATA.START) }
        binding.filter.end.setOnClickListener { toggleSortDirection(binding.filter.a5, DATA.END) }
    }

    private fun toggleSortDirection(imageView: ImageView, targetSortType: String) {
        currentSortType = targetSortType
        if (imageView.tag == "up") {
            fetchData(currentSortType, binding.recyclerView, binding.recyclerViewReverse)
            imageView.tag = "down"
            imageView.setImageResource(R.drawable.ic_down)
        } else {
            fetchData(currentSortType, binding.recyclerViewReverse, binding.recyclerView)
            imageView.tag = "up"
            imageView.setImageResource(R.drawable.ic_up)
        }
    }

    private fun fetchData(orderBy: String, activeRecyclerView: RecyclerView, inactiveRecyclerView: RecyclerView) {
        val uid = DATA.FirebaseUserUid
        FirebaseDatabase.getInstance().getReference(DATA.TASKS)
            .orderByChild(orderBy)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val newList = ArrayList<Task?>()
                    for (snapshot in dataSnapshot.children) {
                        val item = snapshot.getValue(Task::class.java) ?: continue
                        if (item.category == id && item.publisher == uid) {
                            newList.add(item)
                        }
                    }

                    val oldSize = list.size
                    val newSize = newList.size

                    if (list != newList) {
                        list.clear()
                        list.addAll(newList)
                        adapter?.let { adp ->
                            adp.notifyItemRangeRemoved(0, oldSize)
                            adp.notifyItemRangeInserted(0, newSize)
                        }
                    }

                    _binding?.let { b ->
                        b.toolbar.number.text = MessageFormat.format("( {0} )", list.size)
                        inactiveRecyclerView.visibility = View.GONE
                        b.bar.visibility = View.GONE
                        if (list.isNotEmpty()) {
                            activeRecyclerView.visibility = View.VISIBLE
                            b.emptyText.visibility = View.GONE
                        } else {
                            activeRecyclerView.visibility = View.GONE
                            b.emptyText.visibility = View.VISIBLE
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    private fun loadPointsSummary() {
        FirebaseDatabase.getInstance().getReference(DATA.TASKS)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var i = 0
                    var a = 0
                    for (snapshot in dataSnapshot.children) {
                        val item = snapshot.getValue(Task::class.java) ?: continue
                        if (item.category == id) {
                            i += item.points
                            a += item.aVPoints
                        }
                    }
                    _binding?.let { b ->
                        b.allPoints.text = MessageFormat.format("{0}{1}", DATA.EMPTY, i)
                        b.av.text = MessageFormat.format("{0}{1}", DATA.EMPTY, a)
                        b.level.text = MessageFormat.format("{0}{1}", DATA.EMPTY, VOID.levelPoint(a, 10))
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    private fun handleBackPressed() {
        if (searchStatus) {
            binding.toolbar.toolbar.visibility = View.VISIBLE
            binding.toolbar.toolbarSearch.visibility = View.GONE
            searchStatus = false
            binding.toolbar.textSearch.setText(DATA.EMPTY)
        } else {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        fetchData(DATA.TIMESTAMP, binding.recyclerView, binding.recyclerViewReverse)
        loadPointsSummary()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        var searchStatus = false
    }
}