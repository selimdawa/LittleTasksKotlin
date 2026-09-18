package com.flatcode.littletasks.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littletasks.R
import com.flatcode.littletasks.databinding.FragmentSettingsBinding
import com.flatcode.littletasks.model.Setting
import com.flatcode.littletasks.ui.category.CategoriesActivity
import com.flatcode.littletasks.ui.objects.ObjectsActivity
import com.flatcode.littletasks.ui.profile.FavoritesActivity
import com.flatcode.littletasks.ui.profile.ProfileActivity
import com.flatcode.littletasks.ui.profile.ProfileEditActivity
import com.flatcode.littletasks.utils.DATA
import com.flatcode.littletasks.utils.loadImage
import com.flatcode.littletasks.utils.openActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val list = ArrayList<Setting>()
    private var adapter: SettingAdapter? = null
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        initStaticSettings()
        adapter = SettingAdapter(list)
        binding.recyclerView.adapter = adapter

        binding.toolbar.item.setOnClickListener {
            context?.openActivity<ProfileActivity>(
                false, DATA.PROFILE_ID to DATA.firebaseUserUid
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userInfo.collectLatest { user ->
                    user?.let {
                        binding.toolbar.imageProfile.loadImage(true, it.profileImage)
                        binding.toolbar.username.text = it.username
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.pointsSummary.collectLatest { (total, av, level) ->
                    binding.toolbar.all.text = total.toString()
                    binding.toolbar.availablePoints.text = av.toString()
                    binding.toolbar.level.text = level.toString()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.itemCounts.collectLatest { counts ->
                    updateSettingNumber(1, counts[DATA.CATEGORIES] ?: 0)
                    updateSettingNumber(2, counts[DATA.PLANS] ?: 0)
                    updateSettingNumber(3, counts[DATA.OBJECTS] ?: 0)
                    updateSettingNumber(4, counts[DATA.FAVORITES] ?: 0)
                }
            }
        }

        return binding.root
    }

    private fun initStaticSettings() {
        list.clear()
        list.add(
            Setting(
                "1",
                "Edit Profile",
                null,
                R.drawable.ic_edit_white,
                0,
                ProfileEditActivity::class.java
            )
        )
        list.add(
            Setting(
                "2", "Categories", null, R.drawable.ic_category, 0, CategoriesActivity::class.java
            )
        )
        list.add(Setting("4", "Plans", DATA.PLANS, R.drawable.ic_list, 0))
        list.add(
            Setting(
                "7", "Objects", null, R.drawable.ic_object, 0, ObjectsActivity::class.java
            )
        )
        list.add(
            Setting(
                "9",
                "Favorites",
                null,
                R.drawable.ic_star_selected,
                0,
                FavoritesActivity::class.java
            )
        )
        list.add(Setting("10", "About App", null, R.drawable.ic_info, 0))
        list.add(Setting("11", "Logout", null, R.drawable.ic_logout_white, 0))
        list.add(Setting("12", "Share App", null, R.drawable.ic_share, 0))
        list.add(Setting("13", "Rate APP", null, R.drawable.ic_heart_selected, 0))
        list.add(
            Setting(
                "14",
                "Privacy Policy",
                null,
                R.drawable.ic_privacy_policy,
                0,
                PrivacyPolicyActivity::class.java
            )
        )
    }

    private fun updateSettingNumber(index: Int, count: Int) {
        if (index in list.indices && list[index].number != count) {
            list[index].number = count
            adapter?.notifyItemChanged(index)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadUserInfo()
        viewModel.loadPoints()
        viewModel.loadItemCounts()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}