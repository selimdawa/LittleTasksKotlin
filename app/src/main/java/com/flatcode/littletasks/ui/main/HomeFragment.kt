package com.flatcode.littletasks.ui.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.flatcode.littletasks.R
import com.flatcode.littletasks.databinding.FragmentHomeBinding
import com.flatcode.littletasks.ui.profile.FavoritesActivity
import com.flatcode.littletasks.utils.DATA
import com.flatcode.littletasks.utils.openActivity
import com.flatcode.littletasks.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val binding by viewBinding(FragmentHomeBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.one.setOnClickListener {
            context?.openActivity<FavoritesActivity>(false, DATA.TASK_TYPE to DATA.TASKS_ALL)
        }
        binding.two.setOnClickListener {
            context?.openActivity<FavoritesActivity>(
                false, DATA.TASK_TYPE to DATA.TASKS_UN_STARTED
            )
        }
        binding.three.setOnClickListener {
            context?.openActivity<FavoritesActivity>(
                false, DATA.TASK_TYPE to DATA.TASKS_STARTED
            )
        }
        binding.four.setOnClickListener {
            context?.openActivity<FavoritesActivity>(
                false, DATA.TASK_TYPE to DATA.TASKS_COMPLETED
            )
        }
    }
}