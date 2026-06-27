package com.flatcode.littletasks.Adapter

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littletasks.Model.TaskItem
import com.flatcode.littletasks.Unit.DATA
import com.flatcode.littletasks.databinding.ItemNewPlanBinding
import com.google.firebase.database.FirebaseDatabase
import java.text.MessageFormat

class ObjectAddAdapter(private val context: Context, var list: List<TaskItem>) :
    RecyclerView.Adapter<ObjectAddAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNewPlanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val id = position + 1
        val last = list.size - 1

        if (position == last) {
            holder.binding.view.visibility = View.GONE
        } else {
            holder.binding.view.visibility = View.VISIBLE
        }

        holder.binding.number.text = MessageFormat.format("{0}{1}", DATA.EMPTY, id)
        holder.binding.add.setOnClickListener {
            validateData(holder.binding.nameEt, holder.binding.PointsEt, holder.binding.add, holder.binding.done)
        }
        holder.binding.done.setOnClickListener {
            Toast.makeText(context, "Already done", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int = list.size

    class ViewHolder(val binding: ItemNewPlanBinding) : RecyclerView.ViewHolder(binding.root)

    private fun validateData(name: TextView, points: TextView, add: TextView, ok: TextView) {
        val inputName = name.text.toString().trim()
        val inputPoints = points.text.toString().trim()

        if (TextUtils.isEmpty(inputName)) {
            Toast.makeText(context, "Enter Name...", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(inputPoints)) {
            Toast.makeText(context, "Enter Points...", Toast.LENGTH_SHORT).show()
        } else {
            uploadToDB(inputName, inputPoints, add, ok)
        }
    }

    private fun uploadToDB(name: String, points: String, add: TextView, ok: TextView) {
        val ref = FirebaseDatabase.getInstance().getReference(DATA.OBJECTS)
        val id = ref.push().key ?: return
        val point = points.toIntOrNull() ?: 0

        val hashMap = HashMap<String?, Any?>().apply {
            put(DATA.PUBLISHER, DATA.EMPTY + DATA.FirebaseUserUid)
            put(DATA.ID, id)
            put(DATA.NAME, DATA.EMPTY + name)
            put(DATA.POINTS, point)
            put(DATA.TIMESTAMP, System.currentTimeMillis())
        }

        ref.child(id).setValue(hashMap).addOnSuccessListener {
            Toast.makeText(context, "Successfully uploaded...", Toast.LENGTH_SHORT).show()
            add.visibility = View.GONE
            ok.visibility = View.VISIBLE
        }.addOnFailureListener { e ->
            Toast.makeText(
                context, "Failure to upload to db due to :${e.message}", Toast.LENGTH_SHORT
            ).show()
        }
    }
}