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
import com.google.firebase.database.ValueEventListener
import java.text.MessageFormat

class FavoritesActivity : AppCompatActivity() {

    private var _binding: ActivityFavoritesBinding? = null
    private val binding get() = _binding!!

    private val context: Context = this@FavoritesActivity
    private val favoriteItemIds = ArrayList<String>()
    private val list = ArrayList<Task?>()
    private var adapter: TaskAdapter? = null
    private var currentSortType = DATA.TIMESTAMP
    private var tasksType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        THEME.setThemeOfApp(context)
        super.onCreate(savedInstanceState)
        _binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tasksType = intent.getStringExtra(DATA.TASK_TYPE) ?: DATA.TASKS_ALL
        currentSortType = DATA.TIMESTAMP

        binding.toolbar.nameSpace.setText(R.string.favorites)
        binding.toolbar.back.setOnClickListener { handleBackPressed() }
        binding.toolbar.close.setOnClickListener { handleBackPressed() }

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

        adapter = TaskAdapter(context, list)
        binding.recyclerView.adapter = adapter
        binding.recyclerViewReverse.adapter = adapter

        binding.filter.all.setOnClickListener {
            currentSortType = DATA.TIMESTAMP
            fetchFavoriteData(currentSortType, binding.recyclerView, binding.recyclerViewReverse)
        }
        binding.filter.points.setOnClickListener {
            toggleSortDirection(binding.filter.a1, DATA.POINTS)
        }
        binding.filter.AVPoints.setOnClickListener {
            toggleSortDirection(binding.filter.a2, DATA.AVAILABLE_POINTS)
        }
        binding.filter.add.setOnClickListener {
            toggleSortDirection(binding.filter.a3, DATA.TIMESTAMP)
        }
        binding.filter.start.setOnClickListener {
            toggleSortDirection(binding.filter.a4, DATA.START)
        }
        binding.filter.end.setOnClickListener {
            toggleSortDirection(binding.filter.a5, DATA.END)
        }
    }

    private fun toggleSortDirection(imageView: ImageView, targetSortType: String) {
        currentSortType = targetSortType
        if (imageView.tag == "up") {
            fetchFavoriteData(currentSortType, binding.recyclerView, binding.recyclerViewReverse)
            imageView.tag = "down"
            imageView.setImageResource(R.drawable.ic_down)
        } else {
            fetchFavoriteData(currentSortType, binding.recyclerViewReverse, binding.recyclerView)
            imageView.tag = "up"
            imageView.setImageResource(R.drawable.ic_up)
        }
    }

    private fun fetchFavoriteData(orderBy: String, activeRecyclerView: RecyclerView, inactiveRecyclerView: RecyclerView) {
        val uid = DATA.FirebaseUserUid
        FirebaseDatabase.getInstance().getReference(DATA.FAVORITES).child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    favoriteItemIds.clear()
                    for (snapshot in dataSnapshot.children) {
                        snapshot.key?.let { favoriteItemIds.add(it) }
                    }
                    fetchTasks(orderBy, activeRecyclerView, inactiveRecyclerView)
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    private fun fetchTasks(orderBy: String, activeRecyclerView: RecyclerView, inactiveRecyclerView: RecyclerView) {
        val uid = DATA.FirebaseUserUid
        FirebaseDatabase.getInstance().getReference(DATA.TASKS)
            .orderByChild(orderBy)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val newList = ArrayList<Task?>()
                    for (snapshot in dataSnapshot.children) {
                        val task = snapshot.getValue(Task::class.java) ?: continue
                        if (task.id in favoriteItemIds && task.publisher == uid) {
                            when (tasksType) {
                                DATA.TASKS_ALL -> newList.add(task)
                                DATA.TASKS_UN_STARTED -> if (task.start == 0L && task.end == 0L) newList.add(task)
                                DATA.TASKS_STARTED -> if (task.start != 0L && task.end == 0L) newList.add(task)
                                DATA.TASKS_COMPLETED -> if (task.start != 0L && task.end != 0L) newList.add(task)
                            }
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
        fetchFavoriteData(DATA.TIMESTAMP, binding.recyclerView, binding.recyclerViewReverse)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}