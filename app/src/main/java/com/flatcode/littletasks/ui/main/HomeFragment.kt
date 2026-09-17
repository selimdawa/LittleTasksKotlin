package com.flatcode.littletasks.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.flatcode.littletasks.utils.DATA
import com.flatcode.littletasks.utils.openActivity
import com.flatcode.littletasks.databinding.FragmentHomeBinding
import com.flatcode.littletasks.ui.profile.FavoritesActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.one.setOnClickListener {
            context?.openActivity(FavoritesActivity::class.java, DATA.TASK_TYPE to DATA.TASKS_ALL)
        }
        binding.two.setOnClickListener {
            context?.openActivity(FavoritesActivity::class.java, DATA.TASK_TYPE to DATA.TASKS_UN_STARTED)
        }
        binding.three.setOnClickListener {
            context?.openActivity(FavoritesActivity::class.java, DATA.TASK_TYPE to DATA.TASKS_STARTED)
        }
        binding.four.setOnClickListener {
            context?.openActivity(FavoritesActivity::class.java, DATA.TASK_TYPE to DATA.TASKS_COMPLETED)
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}