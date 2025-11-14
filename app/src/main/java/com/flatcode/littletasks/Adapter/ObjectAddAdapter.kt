package com.flatcode.littletasks.Adapter

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littletasks.Model.OBJECT
import com.flatcode.littletasks.Unit.DATA
import com.flatcode.littletasks.databinding.ItemNewPlanBinding
import com.google.firebase.database.FirebaseDatabase
import java.text.MessageFormat

class ObjectAddAdapter(private val context: Context, var list: List<OBJECT>) :
    RecyclerView.Adapter<ObjectAddAdapter.ViewHolder>() {

    private var binding: ItemNewPlanBinding? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = ItemNewPlanBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding!!.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val id = position + 1
        val last = list.size - 1

        if (position == last) {
            holder.line.visibility = View.GONE
        } else {
            holder.line.visibility = View.VISIBLE
        }

        holder.number.text = MessageFormat.format("{0}{1}", DATA.EMPTY, id)
        holder.add.setOnClickListener {
            validateData(holder.name, holder.points, holder.add, holder.done)
        }
        holder.done.setOnClickListener {
            Toast.makeText(context, "Already done", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(view: View?) : RecyclerView.ViewHolder(view!!) {
        var add: TextView
        var number: TextView
        var done: TextView
        var name: EditText
        var points: EditText
        var line: View

        init {
            name = binding!!.nameEt
            add = binding!!.add
            done = binding!!.done
            number = binding!!.number
            points = binding!!.PointsEt
            line = binding!!.view
        }
    }

    private fun validateData(name: TextView, points: TextView, add: TextView, ok: TextView) {
        val Name = name.text.toString().trim { it <= ' ' }
        val Points = points.text.toString().trim { it <= ' ' }

        //validate data
        if (TextUtils.isEmpty(Name)) {
            Toast.makeText(context, "Enter Name...", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(Points)) {
            Toast.makeText(context, "Enter Points...", Toast.LENGTH_SHORT).show()
        } else {
            uploadToDB(Name, Points, add, ok)
        }
    }

    private fun uploadToDB(name: String, points: String, add: TextView, ok: TextView) {
        val ref = FirebaseDatabase.getInstance().getReference(DATA.OBJECTS)
        val id = ref.push().key
        val point = points.toInt()
        //setup data to upload
        val hashMap = HashMap<String?, Any?>()
        hashMap[DATA.PUBLISHER] = DATA.EMPTY + DATA.FirebaseUserUid
        hashMap[DATA.ID] = id
        hashMap[DATA.NAME] = DATA.EMPTY + name
        hashMap[DATA.POINTS] = point
        hashMap[DATA.TIMESTAMP] = System.currentTimeMillis()
        assert(id != null)
        ref.child(id!!).setValue(hashMap).addOnSuccessListener {
            Toast.makeText(context, "Successfully uploaded...", Toast.LENGTH_SHORT).show()
            add.visibility = View.GONE
            ok.visibility = View.VISIBLE
        }.addOnFailureListener { e: Exception ->
            Toast.makeText(
                context, "Failure to upload to db due to :" + e.message, Toast.LENGTH_SHORT
            ).show()
        }
    }
}