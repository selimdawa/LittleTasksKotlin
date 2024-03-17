package com.flatcode.littletasks.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littletasks.Model.Setting
import com.flatcode.littletasks.Unit.CLASS
import com.flatcode.littletasks.Unit.DATA
import com.flatcode.littletasks.Unit.VOID
import com.flatcode.littletasks.databinding.ItemSettingBinding
import java.text.MessageFormat

class SettingAdapter(private val context: Context?, private val list: ArrayList<Setting>) :
    RecyclerView.Adapter<SettingAdapter.ViewHolder>() {

    private var binding: ItemSettingBinding? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = ItemSettingBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding!!.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val id = DATA.EMPTY + item.id
        val name = DATA.EMPTY + item.name
        val image = item.image
        val number = item.number
        val to = item.c
        val type = item.type

        holder.name.text = name
        holder.image.setImageResource(image)

        if (number != 0) {
            holder.number.visibility = View.VISIBLE
            holder.number.text = MessageFormat.format("{0}{1}", DATA.EMPTY, number)
        } else {
            holder.number.visibility = View.GONE
        }

        holder.item.setOnClickListener {
            if (type != null) {
                if (type == DATA.PLANS) {
                    VOID.IntentExtra(context, CLASS.PLANS, DATA.NEW_PLAN, DATA.EMPTY + false)
                } else if (type == DATA.CHOOSE_PLAN) {
                    VOID.IntentExtra(context, CLASS.PLANS, DATA.NEW_PLAN, DATA.EMPTY + true)
                }
            } else {
                when (id) {
                    "10" -> VOID.dialogAboutApp(context)
                    "11" -> VOID.dialogLogout(context)
                    "12" -> VOID.shareApp(context)
                    "13" -> VOID.rateApp(context)
                    else -> VOID.Intent1(context, to)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView
        var number: TextView
        var image: ImageView
        var item: LinearLayout

        init {
            image = binding!!.image
            name = binding!!.name
            number = binding!!.number
            item = binding!!.item
        }
    }
}