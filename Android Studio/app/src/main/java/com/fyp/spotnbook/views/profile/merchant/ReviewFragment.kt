package com.fyp.spotnbook.views.profile.merchant

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fyp.spotnbook.R
import com.fyp.spotnbook.adapter.ClosingDateAdapter
import com.fyp.spotnbook.adapter.MerchantReviewAdapter
import com.fyp.spotnbook.databinding.FragmentReviewBinding
import com.fyp.spotnbook.model.Merchant
import com.fyp.spotnbook.repository.UpdateProfileResult
import com.fyp.spotnbook.viewmodel.AuthenticationViewModel
import com.fyp.spotnbook.viewmodel.ProfileViewModel

class ReviewFragment(private val uid:String) : Fragment() {
    private lateinit var binding: FragmentReviewBinding
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var authViewModel: AuthenticationViewModel

    private lateinit var calculatedRatingScore: TextView
    private lateinit var pb1Star: ProgressBar
    private lateinit var pb2Star: ProgressBar
    private lateinit var pb3Star: ProgressBar
    private lateinit var pb4Star: ProgressBar
    private lateinit var pb5Star: ProgressBar
    private lateinit var btnAddReview: Button
    private lateinit var reviewRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_review, container, false)
        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        authViewModel = ViewModelProvider(this)[AuthenticationViewModel::class.java]
        profileViewModel.getMerchantData(uid)

        binding.apply {
            this@ReviewFragment.calculatedRatingScore = calculatedRatingScore
            pb1Star = progressBar1star
            pb2Star = progressBar2star
            pb3Star = progressBar3star
            pb4Star = progressBar4star
            pb5Star = progressBar5star
            btnAddReview = btnAddRating
            this@ReviewFragment.reviewRecyclerView = reviewRecyclerView
        }

        //-----Observers-----
        profileViewModel.merchantData.observe(viewLifecycleOwner){ merchant ->
            val reviews = merchant.reviews

            val adapter = MerchantReviewAdapter(reviews)
            reviewRecyclerView.adapter = adapter
            reviewRecyclerView.layoutManager = LinearLayoutManager(requireContext())

            adapter.updateData(reviews)

            if (reviews.isNotEmpty()) {
                pb1Star.progress = ((reviews.count { it.stars == 1 }.toDouble() / reviews.size) * 100).toInt()
                pb2Star.progress = ((reviews.count { it.stars == 2 }.toDouble() / reviews.size) * 100).toInt()
                pb3Star.progress = ((reviews.count { it.stars == 3 }.toDouble() / reviews.size) * 100).toInt()
                pb4Star.progress = ((reviews.count { it.stars == 4 }.toDouble() / reviews.size) * 100).toInt()
                pb5Star.progress = ((reviews.count { it.stars == 5 }.toDouble() / reviews.size) * 100).toInt()
            } else {
                pb1Star.progress = 0
                pb2Star.progress = 0
                pb3Star.progress = 0
                pb4Star.progress = 0
                pb5Star.progress = 0
            }

            //Calculate Average
            val totalReviews = reviews.size
            var totalStars = 0

            for (review in reviews) {
                totalStars += review.stars
            }

            val averageRatingScore = if (totalReviews > 0) {
                totalStars.toDouble() / totalReviews
            } else {
                0.0
            }

            calculatedRatingScore.text = if (averageRatingScore == 0.0) "-" else String.format("%.2f", averageRatingScore)



            //-----UI Events-----
            btnAddReview.visibility = if (merchant.reviews.any { it.userUid == authViewModel.currentUser() } || uid == authViewModel.currentUser()) View.GONE else View.VISIBLE
            btnAddReview.setOnClickListener { handleAddReview() }
        }

        return binding.root
    }

    private fun handleAddReview(){
        val intent = Intent(requireContext(), AddMerchantReviewActivity::class.java)
        intent.putExtra("uid", uid)
        startActivity(intent)
    }
}