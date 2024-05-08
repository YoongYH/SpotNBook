package com.fyp.spotnbook.views.profile.merchant

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.fyp.spotnbook.R
import com.fyp.spotnbook.databinding.ActivityBusinessHourBinding
import com.fyp.spotnbook.viewmodel.AuthenticationViewModel
import com.fyp.spotnbook.viewmodel.ProfileViewModel
import java.time.ZoneId
import java.time.ZonedDateTime

class BusinessHourActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBusinessHourBinding
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var authenticationViewModel: AuthenticationViewModel

    private lateinit var mondayBusinessHour:TextView
    private lateinit var tuesdayBusinessHour:TextView
    private lateinit var wednesdayBusinessHour:TextView
    private lateinit var thursdayBusinessHour:TextView
    private lateinit var fridayBusinessHour:TextView
    private lateinit var saturdayBusinessHour:TextView
    private lateinit var sundayBusinessHour:TextView

    private lateinit var btnBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_business_hour)
        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        authenticationViewModel = ViewModelProvider(this)[AuthenticationViewModel::class.java]

        binding.apply {

            this@BusinessHourActivity.mondayBusinessHour = mondayBusinessHour
            this@BusinessHourActivity.tuesdayBusinessHour = tuesdayBusinessHour
            this@BusinessHourActivity.wednesdayBusinessHour = wednesdayBusinessHour
            this@BusinessHourActivity.thursdayBusinessHour = thursdayBusinessHour
            this@BusinessHourActivity.fridayBusinessHour = fridayBusinessHour
            this@BusinessHourActivity.saturdayBusinessHour = saturdayBusinessHour
            this@BusinessHourActivity.sundayBusinessHour = sundayBusinessHour
            this@BusinessHourActivity.btnBack = btnBack
        }

        //Get UID to View Profile
        val data = intent?.getStringExtra("merchantId") // Replace "key" with the same key used when sending the data
        if (data != null) {
            profileViewModel.getMerchantData(data)
        }

        //----------Observers----------
        profileViewModel.merchantData.observe(this) { merchant ->
            val openingHours = merchant.operatingHours.openingHours
            val closingHours = merchant.operatingHours.closingHours

            val dayTextViewMap = mapOf(
                "MONDAY" to mondayBusinessHour,
                "TUESDAY" to tuesdayBusinessHour,
                "WEDNESDAY" to wednesdayBusinessHour,
                "THURSDAY" to thursdayBusinessHour,
                "FRIDAY" to fridayBusinessHour,
                "SATURDAY" to saturdayBusinessHour,
                "SUNDAY" to sundayBusinessHour
            )

            for ((day, textView) in dayTextViewMap) {
                val openingHour = openingHours[day]
                val closingHour = closingHours[day]

                if (openingHour.isNullOrBlank() && closingHour.isNullOrBlank()){
                    textView.text = "Closed"
                }
                else{
                    textView.text = setBusinessHoursText(textView.context, openingHour, closingHour)
                }
            }
        }

        //----------UI Events----------
        btnBack.setOnClickListener{ finish() }
    }

    fun setBusinessHoursText(context: Context, openingHour: String?, closingHour: String?): String {
        return context.getString(R.string.business_hours_placeholder, openingHour ?: "", closingHour ?: "")
    }
}