package com.fyp.spotnbook.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.fyp.spotnbook.repository.DisplayType
import com.fyp.spotnbook.views.posts.PostFragment

class HomeTabAdapter(fragment: Fragment, uid: String) : FragmentStateAdapter(fragment) {
    private val fragmentList = listOf(
        PostFragment.newInstance(DisplayType.RECOMMEND, uid),
        PostFragment.newInstance(DisplayType.FOLLOWING, uid)
    )

    override fun getItemCount(): Int = fragmentList.size

    override fun createFragment(position: Int): Fragment = fragmentList[position]
}
