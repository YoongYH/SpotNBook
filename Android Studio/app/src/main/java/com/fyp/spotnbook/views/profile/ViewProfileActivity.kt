package com.fyp.spotnbook.views.profile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.fyp.spotnbook.R
import com.fyp.spotnbook.databinding.ActivityViewProfileBinding
import com.fyp.spotnbook.repository.UserType
import com.fyp.spotnbook.viewmodel.ProfileViewModel
import com.fyp.spotnbook.views.profile.merchant.MerchantProfileFragment
import com.fyp.spotnbook.views.profile.user.UserProfileFragment

class ViewProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewProfileBinding
    private lateinit var profileViewModel: ProfileViewModel

    private lateinit var fragmentContainer: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_view_profile)
        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        binding.apply {
            this@ViewProfileActivity.fragmentContainer = fragmentContainer
        }

        val uidToView = intent.getStringExtra("uid").toString()
        profileViewModel.checkUserType(uidToView)

        //-----Observers-----
        profileViewModel.userType.observe(this) { result ->
            when (result) {
                UserType.USER -> {
                    val userFragment = UserProfileFragment()
                    val bundle = Bundle()
                    bundle.putString("uid", uidToView)
                    userFragment.arguments = bundle

                    val fragmentManager = supportFragmentManager
                    val fragmentTransaction = fragmentManager.beginTransaction()
                    fragmentTransaction.replace(fragmentContainer.id, userFragment)
                    fragmentTransaction.commit()
                }
                UserType.MERCHANT -> {
                    val merchantFragment = MerchantProfileFragment()
                    val bundle = Bundle()
                    bundle.putString("uid", uidToView)
                    merchantFragment.arguments = bundle

                    val fragmentManager = supportFragmentManager
                    val fragmentTransaction = fragmentManager.beginTransaction()
                    fragmentTransaction.replace(fragmentContainer.id, merchantFragment)
                    fragmentTransaction.commit()
                }
                else -> {
                    Toast.makeText(this, "No User Profile Found.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}