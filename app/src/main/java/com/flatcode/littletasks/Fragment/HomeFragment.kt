package com.flatcode.littletasks.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.flatcode.littletasks.Unit.CLASS
import com.flatcode.littletasks.Unit.DATA
import com.flatcode.littletasks.Unit.VOID
import com.flatcode.littletasks.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentHomeBinding.inflate(LayoutInflater.from(context), container, false)
        binding.one.setOnClickListener {
            VOID.IntentExtra(context, CLASS.FAVORITES, DATA.TASK_TYPE, DATA.TASKS_ALL)
        }
        binding.two.setOnClickListener {
            VOID.IntentExtra(context, CLASS.FAVORITES, DATA.TASK_TYPE, DATA.TASKS_UN_STARTED)
        }
        binding.three.setOnClickListener {
            VOID.IntentExtra(context, CLASS.FAVORITES, DATA.TASK_TYPE, DATA.TASKS_STARTED)
        }
        binding.four.setOnClickListener {
            VOID.IntentExtra(context, CLASS.FAVORITES, DATA.TASK_TYPE, DATA.TASKS_COMPLETED)
        }
        return binding.root
    }
}