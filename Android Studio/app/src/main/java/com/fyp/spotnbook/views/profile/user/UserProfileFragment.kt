package com.fyp.spotnbook.views.profile.user

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.fyp.spotnbook.R
import com.fyp.spotnbook.adapter.UserProfilePageTabAdapter
import com.fyp.spotnbook.databinding.FragmentUserProfileBinding
import com.fyp.spotnbook.viewmodel.AuthenticationViewModel
import com.fyp.spotnbook.viewmodel.PostViewModel
import com.fyp.spotnbook.viewmodel.ProfileViewModel
import com.fyp.spotnbook.views.authentication.LoginActivity
import com.fyp.spotnbook.views.profile.ChangePasswordActivity
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.tabs.TabLayout

class UserProfileFragment : Fragment() {
    private lateinit var binding: FragmentUserProfileBinding
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var authenticationViewModel: AuthenticationViewModel
    private lateinit var postViewModel: PostViewModel

    private lateinit var user_tab: TabLayout
    private lateinit var user_viewpage: ViewPager2

    private lateinit var username: TextView
    private lateinit var profileUid: TextView
    private lateinit var profilePicture: ShapeableImageView
    private lateinit var numFollowers: TextView
    private lateinit var numFollowing: TextView
    private lateinit var numPosts: TextView
    private lateinit var bio: TextView
    private lateinit var genderContainer: CardView
    private lateinit var genderTextView: TextView
    private lateinit var stateContainer: CardView
    private lateinit var stateTextView: TextView
    private lateinit var followContainer: CardView
    private lateinit var followTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_user_profile, container, false
        )

        binding.apply {
            lifecycleOwner = viewLifecycleOwner

            //UI Components
            username = profileUsername
            profileUid = profileUID
            this@UserProfileFragment.profilePicture = profilePicture
            numFollowers = numberFollowers
            numFollowing = numberFollowing
            numPosts = numberPost
            bio = profileBio
            genderContainer = genderCardView
            this@UserProfileFragment.genderTextView = genderTextView
            stateContainer = stateCardView
            this@UserProfileFragment.stateTextView = stateTextView
            followContainer = followCardView
            this@UserProfileFragment.followTextView = followTextView

            //User Tabs
            user_tab = userProfileTabLayout
            user_viewpage = userProfileViewPager
        }

        //-----ViewModels-----
        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        authenticationViewModel = ViewModelProvider(this)[AuthenticationViewModel::class.java]
        postViewModel = ViewModelProvider(this)[PostViewModel::class.java]

        val uid= (arguments?.getString("uid") ?: authenticationViewModel.currentUser()).toString()
        profileViewModel.getUserData(uid)

        //NavDrawer Visibility Logic
        binding.profileNavDrawer.visibility = if (uid == authenticationViewModel.currentUser().toString()) View.VISIBLE else View.GONE

        //----------User Data Observers----------
        profileViewModel.userData.observe(viewLifecycleOwner) { user ->
            // Assign Value
            username.text = user.username
            profileUid.text = "UID: " + user.userID

            bio.text = user.bio
            bio.visibility = if (user.bio.isBlank()) View.GONE else View.VISIBLE

            genderTextView.text = user.gender.value
            stateTextView.text = user.state.value

            //Profile Image
            Glide.with(this)
                .load(user.imageUrl)
                .centerCrop()
                .placeholder(R.drawable.anonymous)
                .error(R.drawable.anonymous)
                .into(profilePicture)

            // Followers Number Display
            numFollowers.text = user.followedBy.size.toString()
            numFollowing.text = user.following.size.toString()

            // Posts Number Display
            postViewModel.postList.observe(viewLifecycleOwner) { postList ->
                numPosts.text = postList?.count { it.postBy == user.userID }.toString() ?: "0"
            }

            // Gender Visibility Logic
            val genderVisibility =
                if (!user.gender.display || user.gender.value == "Prefer not to Tell") View.GONE else View.VISIBLE
            genderContainer.visibility = genderVisibility

            if (genderVisibility == View.VISIBLE) {
                val genderColorResId = when (user.gender.value) {
                    "Male" -> R.color.male
                    "Female" -> R.color.female
                    else -> R.color.input_border
                }

                genderContainer.setCardBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        genderColorResId
                    )
                )
            }

            // State Visibility Logic
            val stateVisibility =
                if (!user.state.display || user.state.value == "Prefer not to Tell") View.GONE else View.VISIBLE
            stateContainer.visibility = stateVisibility

            followContainer.setOnClickListener {
                if (uid == authenticationViewModel.currentUser()) {
                    Toast.makeText(
                        requireContext(),
                        "You cant follow yourself.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    profileViewModel.followProfile(uid)
                }
            }

            if (user.followedBy.contains(authenticationViewModel.currentUser().toString())) {
                followTextView.text = "Following"
                followContainer.setCardBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.input_border
                    )
                )
            } else {
                followTextView.text = "Follow"
                followContainer.setCardBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.primary
                    )
                )
            }
        }

        //----------Tab Switch----------
        // Initialize Adapter
        user_viewpage.adapter = UserProfilePageTabAdapter(this, uid)

        // Add Tabs
        user_tab.addTab(user_tab.newTab().setText("Posts"))
        user_tab.addTab(user_tab.newTab().setText("Liked"))

        // Tab Layout Listener
        user_tab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let { user_viewpage.currentItem = it.position }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

        // Page Change Listener
        user_viewpage.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                user_tab.getTabAt(position)?.select()
            }
        })

        //----------Navigation Drawer----------
        // Display Navigation Drawer when drawer icon is clicked
        binding.profileNavDrawer.setOnClickListener {
            binding.profileDrawerLayout.openDrawer(GravityCompat.END)
        }

        // Set Logout Item Text Color to Red
        setLogoutItemTextColor()

        // Initialize ActionBarDrawerToggle and sync state
        val drawerlayout = binding.profileDrawerLayout
        val toggle = ActionBarDrawerToggle(requireActivity(), drawerlayout, R.string.profile, R.string.home)
        drawerlayout.addDrawerListener(toggle)
        toggle.syncState()

        //Remove unnecessary item for User
        binding.navView.menu.removeItem(R.id.nav_merchantCentre)

        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_editProfile -> {
                    startActivity(Intent(requireContext(), EditProfileActivity::class.java))
                    true
                }
                R.id.nav_changePassword -> {
                    startActivity(Intent(requireContext(), ChangePasswordActivity::class.java))
                    true
                }
                R.id.nav_logOut -> {
                    logout()
                    true
                }
                else -> false
            }
        }

        //----------UI Events----------
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        Glide.with(this).clear(profilePicture)
    }

    private fun setLogoutItemTextColor() {
        val navView = binding.navView
        val logoutItem = navView.menu.findItem(R.id.nav_logOut)

        val spannable = SpannableString(logoutItem.title)
        spannable.setSpan(ForegroundColorSpan(Color.RED), 0, spannable.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        logoutItem.title = spannable
    }

    private fun logout() {
        authenticationViewModel.logoutUser()

        // Intent to Login Page
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        requireActivity().finish()

        Toast.makeText(requireContext(), "Logout Successful.", Toast.LENGTH_SHORT).show()
    }
}