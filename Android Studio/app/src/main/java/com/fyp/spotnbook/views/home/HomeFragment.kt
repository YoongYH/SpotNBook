package com.fyp.spotnbook.views.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.fyp.spotnbook.R
import com.fyp.spotnbook.adapter.HomeTabAdapter
import com.fyp.spotnbook.databinding.FragmentHomeBinding
import com.fyp.spotnbook.views.authentication.LoginActivity
import com.google.android.material.tabs.TabLayout

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding

    private lateinit var home_tab: TabLayout
    private lateinit var home_viewpage: ViewPager2
    private lateinit var home_tab_adapter: HomeTabAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_home, container, false
        )
        binding.lifecycleOwner = viewLifecycleOwner

        //HomeTabLayout
        home_tab = binding.homeTabLayout
        home_viewpage = binding.homeViewPost
        home_tab_adapter = HomeTabAdapter(this, "")

        //Adding Three Tab into TabLayout
        home_tab.addTab(home_tab.newTab().setText("Recommended"))
        home_tab.addTab(home_tab.newTab().setText("Following"))

        home_viewpage.adapter = home_tab_adapter

        home_tab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    home_viewpage.currentItem = tab.position
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

        home_viewpage.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                home_tab.selectTab(home_tab.getTabAt(position))
            }
        })

        //Search OnClick Event
        binding.searchBar.setOnClickListener{
            val intent = Intent(requireContext(), SearchActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }
}