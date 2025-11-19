package com.surya.parfum

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.surya.parfum.databinding.ActivityOrderHistoryBinding

class OrderHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topAppBar)
        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }

        setupViewPager()
    }

    private fun setupViewPager() {
        // Menggunakan Adapter ViewPager
        val adapter = OrderPagerAdapter(this)
        binding.viewPager.adapter = adapter

        // Hubungkan Tab dengan ViewPager
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Dalam Proses" // Tab 1
                1 -> tab.text = "Riwayat"      // Tab 2
            }
        }.attach()
    }

    // Inner Class Adapter untuk ViewPager
    private inner class OrderPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> OrderListFragment.newInstance("active") // Kirim filter 'active'
                1 -> OrderListFragment.newInstance("history") // Kirim filter 'history'
                else -> OrderListFragment.newInstance("active")
            }
        }
    }
}