package com.flatcode.littletasks.Adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littletasks.Model.Setting
import com.flatcode.littletasks.Unit.CLASS
import com.flatcode.littletasks.Unit.DATA
import com.flatcode.littletasks.Unit.VOID
import com.flatcode.littletasks.databinding.ItemSettingBinding
import java.text.MessageFormat

class SettingAdapter(private val context: Context?, private val list: ArrayList<Setting>) :
    RecyclerView.Adapter<SettingAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSettingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val id = DATA.EMPTY + item.id
        val name = DATA.EMPTY + item.name
        val image = item.image
        val number = item.number
        val to = item.c
        val type = item.type

        holder.binding.name.text = name
        holder.binding.image.setImageResource(image)

        if (number != 0) {
            holder.binding.number.visibility = View.VISIBLE
            holder.binding.number.text = MessageFormat.format("{0}{1}", DATA.EMPTY, number)
        } else {
            holder.binding.number.visibility = View.GONE
        }

        holder.binding.item.setOnClickListener {
            if (type != null) {
                if (type == DATA.PLANS) {
                    VOID.IntentExtra(context, CLASS.PLANS, DATA.NEW_PLAN, DATA.EMPTY + false)
                } else if (type == DATA.CHOOSE_PLAN) {
                    VOID.IntentExtra(context, CLASS.PLANS, DATA.NEW_PLAN, DATA.EMPTY + true)
                }
            } else {
                when (id) {
                    "10" -> VOID.dialogAboutApp(context as? Activity)
                    "11" -> VOID.dialogLogout(context as? Activity)
                    "12" -> VOID.shareApp(context)
                    "13" -> VOID.rateApp(context)
                    else -> VOID.Intent1(context, to)
                }
            }
        }
    }

    override fun getItemCount(): Int = list.size

    class ViewHolder(val binding: ItemSettingBinding) : RecyclerView.ViewHolder(binding.root)
}