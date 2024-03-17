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
import com.flatcode.littletasks.Unit.DATA
import com.flatcode.littletasks.Unit.THEME
import com.flatcode.littletasks.databinding.ActivityFavoritesBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import java.text.MessageFormat

class FavoritesActivity : AppCompatActivity() {

    private var binding: ActivityFavoritesBinding? = null
    private val context: Context = this@FavoritesActivity
    var item: MutableList<String?>? = null
    var list: ArrayList<Task?>? = null
    var adapter: TaskAdapter? = null
    var type: String? = null
    var tasksType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        THEME.setThemeOfApp(context)
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        tasksType = intent.getStringExtra(DATA.TASK_TYPE)

        if (tasksType == null) tasksType = DATA.TASKS_ALL
        type = DATA.TIMESTAMP

        binding!!.toolbar.nameSpace.setText(R.string.favorites)
        binding!!.toolbar.back.setOnClickListener { onBackPressed() }
        binding!!.toolbar.close.setOnClickListener { onBackPressed() }

        binding!!.toolbar.search.setOnClickListener {
            binding!!.toolbar.toolbar.visibility = View.GONE
            binding!!.toolbar.toolbarSearch.visibility = View.VISIBLE
            DATA.searchStatus = true
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
        binding!!.filter.points.setOnClickListener {
            UpToDown(binding!!.filter.a1, DATA.POINTS)
        }
        binding!!.filter.AVPoints.setOnClickListener {
            UpToDown(binding!!.filter.a2, DATA.AVAILABLE_POINTS)
        }
        binding!!.filter.add.setOnClickListener {
            UpToDown(binding!!.filter.a3, DATA.TIMESTAMP)
        }
        binding!!.filter.start.setOnClickListener {
            UpToDown(binding!!.filter.a4, DATA.START)
        }
        binding!!.filter.end.setOnClickListener {
            UpToDown(binding!!.filter.a5, DATA.END)
        }
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
        item = ArrayList()
        val reference = FirebaseDatabase.getInstance().getReference(DATA.FAVORITES)
            .child(DATA.FirebaseUserUid)
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                (item as ArrayList<String?>).clear()
                for (snapshot in dataSnapshot.children) {
                    (item as ArrayList<String?>).add(snapshot.key)
                }
                getItems(orderBy, recyclerView, recyclerView2)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun getItems(orderBy: String?, recyclerView: RecyclerView, RV2: RecyclerView) {
        val ref: Query = FirebaseDatabase.getInstance().getReference(DATA.TASKS)
        ref.orderByChild(orderBy!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                list!!.clear()
                var i = 0
                for (snapshot in dataSnapshot.children) {
                    val task = snapshot.getValue(Task::class.java)
                    for (id in item!!) {
                        assert(task != null)
                        if (task!!.id == id) if (task.publisher == DATA.FirebaseUserUid)
                            if (tasksType == DATA.TASKS_ALL) {
                                list!!.add(task)
                                i++
                            } else if (tasksType == DATA.TASKS_UN_STARTED) {
                                if (task.start == 0L && task.end == 0L) {
                                    list!!.add(task)
                                    i++
                                } else if (tasksType == DATA.TASKS_STARTED) {
                                    if (task.start != 0L && task.end == 0L) {
                                        list!!.add(task)
                                        i++
                                    }
                                } else if (tasksType == DATA.TASKS_COMPLETED) {
                                    if (task.start != 0L && task.end != 0L) {
                                        list!!.add(task)
                                        i++
                                    }
                                }
                            }
                    }
                }
                binding!!.toolbar.number.text = MessageFormat.format("( {0} )", i)
                RV2.visibility = View.GONE
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

    override fun onBackPressed() {
        if (DATA.searchStatus) {
            binding!!.toolbar.toolbar.visibility = View.VISIBLE
            binding!!.toolbar.toolbarSearch.visibility = View.GONE
            DATA.searchStatus = false
            binding!!.toolbar.textSearch.setText(DATA.EMPTY)
        } else super.onBackPressed()
    }

    override fun onResume() {
        super.onResume()
        getData(DATA.TIMESTAMP, binding!!.recyclerView, binding!!.recyclerViewReverse)
    }

    override fun onRestart() {
        super.onRestart()
        getData(DATA.TIMESTAMP, binding!!.recyclerView, binding!!.recyclerViewReverse)
    }
}