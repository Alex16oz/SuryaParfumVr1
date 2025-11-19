package com.surya.parfum

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.surya.parfum.databinding.FragmentAdminOrdersParentBinding

class AdminOrdersParentFragment : Fragment() {

    private var _binding: FragmentAdminOrdersParentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminOrdersParentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup ViewPager dengan Adapter
        val adapter = OrdersPagerAdapter(this)
        binding.viewPager.adapter = adapter

        // Hubungkan TabLayout dengan ViewPager
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Perlu Diproses"
                1 -> tab.text = "Riwayat Pesanan"
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Inner class untuk Adapter ViewPager
    inner class OrdersPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> AdminOrderListFragment.newInstance("active") // Tab 1: Pesanan Aktif
                1 -> AdminOrderListFragment.newInstance("history") // Tab 2: Riwayat
                else -> AdminOrderListFragment.newInstance("active")
            }
        }
    }
}