package com.fyp.spotnbook.views.profile.merchant

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.fyp.spotnbook.R
import com.fyp.spotnbook.databinding.ActivityAddMerchantReviewBinding
import com.fyp.spotnbook.model.MerchantReviews
import com.fyp.spotnbook.repository.UpdateProfileResult
import com.fyp.spotnbook.viewmodel.ProfileViewModel
import com.google.firebase.Timestamp

class AddMerchantReviewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddMerchantReviewBinding
    private lateinit var profileViewModel: ProfileViewModel

    private lateinit var btnBack: ImageView
    private lateinit var star1: ImageView
    private lateinit var star2: ImageView
    private lateinit var star3: ImageView
    private lateinit var star4: ImageView
    private lateinit var star5: ImageView
    private lateinit var reviewText: EditText
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_merchant_review)
        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        val uid = intent.getStringExtra("uid").toString()

        binding.apply {
            this@AddMerchantReviewActivity.btnBack = btnBack
            btnSave = btnSubmitReview
            this@AddMerchantReviewActivity.reviewText = reviewText
            this@AddMerchantReviewActivity.star1 = star1
            this@AddMerchantReviewActivity.star2 = star2
            this@AddMerchantReviewActivity.star3 = star3
            this@AddMerchantReviewActivity.star4 = star4
            this@AddMerchantReviewActivity.star5 = star5
        }

        //-----Observers-----
        profileViewModel.updateProfileResult.observe(this){ result ->
            handleResult(result)
        }

        //-----Review Logic-----
        var starsSelected: Int = 0
        star1.setOnClickListener {
            starsSelected = 1
            star1.setImageResource(R.drawable.icon_star)
            star2.setImageResource(R.drawable.icon_star_grey)
            star3.setImageResource(R.drawable.icon_star_grey)
            star4.setImageResource(R.drawable.icon_star_grey)
            star5.setImageResource(R.drawable.icon_star_grey)
        }
        star2.setOnClickListener {
            starsSelected = 2
            star1.setImageResource(R.drawable.icon_star)
            star2.setImageResource(R.drawable.icon_star)
            star3.setImageResource(R.drawable.icon_star_grey)
            star4.setImageResource(R.drawable.icon_star_grey)
            star5.setImageResource(R.drawable.icon_star_grey)
        }
        star3.setOnClickListener {
            starsSelected = 3
            star1.setImageResource(R.drawable.icon_star)
            star2.setImageResource(R.drawable.icon_star)
            star3.setImageResource(R.drawable.icon_star)
            star4.setImageResource(R.drawable.icon_star_grey)
            star5.setImageResource(R.drawable.icon_star_grey)
        }
        star4.setOnClickListener {
            starsSelected = 4
            star1.setImageResource(R.drawable.icon_star)
            star2.setImageResource(R.drawable.icon_star)
            star3.setImageResource(R.drawable.icon_star)
            star4.setImageResource(R.drawable.icon_star)
            star5.setImageResource(R.drawable.icon_star_grey)
        }
        star5.setOnClickListener {
            starsSelected = 5
            star1.setImageResource(R.drawable.icon_star)
            star2.setImageResource(R.drawable.icon_star)
            star3.setImageResource(R.drawable.icon_star)
            star4.setImageResource(R.drawable.icon_star)
            star5.setImageResource(R.drawable.icon_star)
        }

        btnSave.setOnClickListener {
            val review = MerchantReviews(
                review = reviewText.text.toString(),
                stars = starsSelected,
                reviewOn = Timestamp.now()
            )
            handleSaveButton(uid, review)
        }
        btnBack.setOnClickListener { finish() }
    }

    private fun handleSaveButton(uid:String, review:MerchantReviews){
        profileViewModel.addReview(uid, review)
    }

    private fun handleResult(result: UpdateProfileResult){
        if(result.isSuccess){
            Toast.makeText(this, "Review Added Successfully.", Toast.LENGTH_SHORT).show()
            finish()
        }
        else{
            Toast.makeText(this, result.errorMessage, Toast.LENGTH_SHORT).show()
        }
    }
}