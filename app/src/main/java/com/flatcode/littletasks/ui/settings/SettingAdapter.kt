package com.flatcode.littletasks.ui.settings

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littletasks.databinding.ItemSettingBinding
import com.flatcode.littletasks.model.Setting
import com.flatcode.littletasks.ui.plan.PlansActivity
import com.flatcode.littletasks.utils.DATA
import com.flatcode.littletasks.utils.dialogAboutApp
import com.flatcode.littletasks.utils.dialogLogout
import com.flatcode.littletasks.utils.openActivity
import com.flatcode.littletasks.utils.rateApp
import com.flatcode.littletasks.utils.shareApp
import java.text.MessageFormat

class SettingAdapter(private val list: ArrayList<Setting>) :
    RecyclerView.Adapter<SettingAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSettingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    class ViewHolder(val binding: ItemSettingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Setting) {
            val id = DATA.EMPTY + item.id
            val name = DATA.EMPTY + item.name
            val image = item.image
            val number = item.number
            val to = item.c
            val type = item.type

            binding.name.text = name
            binding.image.setImageResource(image)

            if (number != 0) {
                binding.number.visibility = View.VISIBLE
                binding.number.text = MessageFormat.format("{0}{1}", DATA.EMPTY, number)
            } else {
                binding.number.visibility = View.GONE
            }

            binding.item.setOnClickListener {
                val context = itemView.context
                if (type != null) {
                    if (type == DATA.PLANS) {
                        context.openActivity<PlansActivity>(false, DATA.NEW_PLAN to "false")
                    } else if (type == DATA.CHOOSE_PLAN) {
                        context.openActivity<PlansActivity>(false, DATA.NEW_PLAN to "true")
                    }
                } else {
                    when (id) {
                        "10" -> (context as? Activity)?.dialogAboutApp()
                        "11" -> (context as? Activity)?.dialogLogout()
                        "12" -> context.shareApp()
                        "13" -> context.rateApp()
                        else -> if (to != null) context.startActivity(Intent(context, to))
                    }
                }
            }
        }
    }
}
