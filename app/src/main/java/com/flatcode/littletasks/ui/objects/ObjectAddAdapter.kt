package com.flatcode.littletasks.ui.objects

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littletasks.databinding.ItemNewPlanBinding
import com.flatcode.littletasks.model.TaskItem
import com.flatcode.littletasks.utils.DATA
import java.text.MessageFormat

class ObjectAddAdapter(private val listener: ObjectAddListener, var list: List<TaskItem>) :
    RecyclerView.Adapter<ObjectAddAdapter.ViewHolder>() {

    interface ObjectAddListener {
        fun onAddClick(name: String, points: String, add: View, ok: View)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNewPlanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position, list.size, listener)
    }

    override fun getItemCount(): Int = list.size

    class ViewHolder(val binding: ItemNewPlanBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int, listSize: Int, listener: ObjectAddListener) {
            val id = position + 1
            val last = listSize - 1

            if (position == last) {
                binding.view.visibility = View.GONE
            } else {
                binding.view.visibility = View.VISIBLE
            }

            binding.number.text = MessageFormat.format("{0}{1}", DATA.EMPTY, id)
            binding.add.setOnClickListener {
                val inputName = binding.nameEt.text.toString().trim()
                val inputPoints = binding.PointsEt.text.toString().trim()
                listener.onAddClick(inputName, inputPoints, binding.add, binding.done)
            }
            binding.done.setOnClickListener {
                Toast.makeText(itemView.context, "Already done", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
