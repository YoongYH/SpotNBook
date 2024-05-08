package com.fyp.spotnbook.views.profile.merchant

import android.content.Intent
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.fyp.spotnbook.R
import com.fyp.spotnbook.databinding.ActivityMerchantCentreBinding
import com.fyp.spotnbook.viewmodel.AuthenticationViewModel
import com.fyp.spotnbook.viewmodel.ProfileViewModel

class MerchantCentreActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMerchantCentreBinding
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var authenticationViewModel: AuthenticationViewModel

    private lateinit var btnBack: ImageView
    private lateinit var btnVerification: Button
    private lateinit var btnBusinessHour: Button
    private lateinit var btnSpecialClosing: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_merchant_centre)
        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        authenticationViewModel = ViewModelProvider(this)[AuthenticationViewModel::class.java]
        profileViewModel.getMerchantData(authenticationViewModel.currentUser().toString())

        binding.apply {
            this@MerchantCentreActivity.btnBack = btnBack
            btnVerification = btnMerchantVerification
            btnBusinessHour = btnMerchantBusinessHour
            btnSpecialClosing = btnMerchantClosingDate
        }

        //----------Observers----------
        profileViewModel.merchantData.observe(this) { merchant ->

            //Disable Btn if Completed Authorization
            if (merchant.authorizedMerchant) {
                btnVerification.isEnabled = false
                btnVerification.alpha = 0.4f
                btnVerification.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_competed, 0)
            } else {
                btnVerification.isEnabled = true
                btnVerification.alpha = 1.0f
                btnVerification.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            }
        }

        //----------UI Events----------
        btnBack.setOnClickListener { finish() }
        btnVerification.setOnClickListener { handleButtonClick(it as Button) }
        btnBusinessHour.setOnClickListener { handleButtonClick(it as Button) }
        btnSpecialClosing.setOnClickListener { handleButtonClick(it as Button) }
    }

    //Intent to Different Activity
    private fun handleButtonClick(button: Button) {
        when (button.id) {
            R.id.btnMerchantVerification -> {
                val verificationIntent = Intent(this, MerchantVerificationActivity::class.java)
                startActivity(verificationIntent)
            }
            R.id.btnMerchantBusinessHour -> {
                val businessHourIntent = Intent(this, EditBusinessHourActivity::class.java)
                startActivity(businessHourIntent)
            }
            R.id.btnMerchantClosingDate -> {
                val specialClosingIntent = Intent(this, EditClosingDateActivity::class.java)
                startActivity(specialClosingIntent)
            }
        }
    }
}