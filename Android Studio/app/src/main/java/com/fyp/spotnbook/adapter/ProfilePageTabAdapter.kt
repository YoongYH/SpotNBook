package com.fyp.spotnbook.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.fyp.spotnbook.repository.DisplayType
import com.fyp.spotnbook.views.posts.PostFragment
import com.fyp.spotnbook.views.profile.merchant.ReviewFragment

class UserProfilePageTabAdapter(fragment: Fragment, uid: String) : FragmentStateAdapter(fragment) {

    private val fragmentList = listOf(
        PostFragment.newInstance(DisplayType.USER_POST, uid),
        PostFragment.newInstance(DisplayType.USER_LIKED, uid),
    )

    override fun getItemCount(): Int = fragmentList.size

    override fun createFragment(position: Int): Fragment = fragmentList[position]
}

class MerchantProfilePageTabAdapter(fragment: Fragment, uid: String) : FragmentStateAdapter(fragment) {

    private val fragmentList = listOf(
        ReviewFragment(uid),
        PostFragment.newInstance(DisplayType.USER_POST, uid),
        PostFragment.newInstance(DisplayType.TAGGED_MERCHANT, uid),
    )

    override fun getItemCount(): Int = fragmentList.size

    override fun createFragment(position: Int): Fragment = fragmentList[position]
}