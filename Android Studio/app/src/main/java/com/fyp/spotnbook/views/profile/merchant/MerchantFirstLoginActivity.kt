package com.fyp.spotnbook.views.profile.merchant

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.fyp.spotnbook.R
import com.fyp.spotnbook.databinding.ActivityMerchantFirstLoginBinding
import com.fyp.spotnbook.viewmodel.AuthenticationViewModel
import com.fyp.spotnbook.viewmodel.ProfileViewModel

class MerchantFirstLoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMerchantFirstLoginBinding
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var authenticationViewModel: AuthenticationViewModel

    private lateinit var btnEditProfile: Button
    private lateinit var btnAddBusinessHour: Button
    private lateinit var btnVerify: Button
    private lateinit var btnStart: Button

    private var btnEditProfileDone: Boolean = false
    private var btnAddBusinessHourDone: Boolean = false
    private var btnVerifyDone: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_merchant_first_login)
        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        authenticationViewModel = ViewModelProvider(this)[AuthenticationViewModel::class.java]
        profileViewModel.getMerchantData(authenticationViewModel.currentUser().toString())

        binding.apply {
            btnEditProfile = btnToEditProfile
            btnAddBusinessHour = btnToAddBusinessHour
            btnVerify = btnToVerify
            this@MerchantFirstLoginActivity.btnStart = btnStart
        }

        profileViewModel.merchantData.observe(this){ merchant ->
            //Define Opening Hours Done Logic
            val openingHours = merchant.operatingHours.openingHours
            val closingHours = merchant.operatingHours.closingHours

            if(!openingHours.isEmpty() && !closingHours.isEmpty()){
                btnAddBusinessHourDone = true;
                btnAddBusinessHour.isClickable = false;
                btnAddBusinessHour.backgroundTintList = ContextCompat.getColorStateList(this, R.color.secondary)
                val drawable = ContextCompat.getDrawable(this, R.drawable.icon_competed)
                btnAddBusinessHour.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
            }

            //Define Edit Profile Done Logic
            val merchantContact = merchant.contactNumber
            val merchantWebsite = merchant.website
            val merchantAddress = merchant.address
            val merchantType = merchant.businessType

            if(!merchantContact.isBlank() || !merchantWebsite.isBlank() || !merchantAddress.isBlank() || !merchantType.isBlank()){
                btnEditProfileDone = true;
                btnEditProfile.isClickable = false;
                btnEditProfile.backgroundTintList = ContextCompat.getColorStateList(this, R.color.secondary)
                val drawable = ContextCompat.getDrawable(this, R.drawable.icon_competed)
                btnEditProfile.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
            }

            //Define Verification Logic
            val merchantVerify = merchant.authorizedMerchant

            if(merchantVerify){
                btnVerifyDone = true;
                btnVerify.isClickable = false;
                btnVerify.backgroundTintList = ContextCompat.getColorStateList(this, R.color.secondary)
                val drawable = ContextCompat.getDrawable(this, R.drawable.icon_competed)
                btnVerify.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
            }

            if(btnEditProfileDone && btnAddBusinessHourDone){
                btnStart.isEnabled = true
            }
        }

        setButtonClickListener(btnEditProfile, EditMerchantProfileActivity::class.java)
        setButtonClickListener(btnAddBusinessHour, EditBusinessHourActivity::class.java)
        setButtonClickListener(btnVerify, MerchantVerificationActivity::class.java)
        btnStart.setOnClickListener {
            Toast.makeText(this, "Welcome to SpotNBook!", Toast.LENGTH_SHORT).show()
            profileViewModel.firstLoginComplete()
            finish()
        }
    }

    private fun setButtonClickListener(button: Button, destinationActivity: Class<*>) {
        button.setOnClickListener {
            val intent = Intent(this, destinationActivity)
            startActivity(intent)
        }
    }
}