package com.fyp.spotnbook.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fyp.spotnbook.R
import com.fyp.spotnbook.model.MerchantReviews
import com.fyp.spotnbook.repository.ProfileRepository
import com.fyp.spotnbook.repository.UserType
import com.google.android.material.imageview.ShapeableImageView
import java.text.SimpleDateFormat
import java.util.Locale

class MerchantReviewAdapter(
    private var reviewList: List<MerchantReviews>
) : RecyclerView.Adapter<MerchantReviewAdapter.ViewHolder>() {

    private var profileRepository = ProfileRepository()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ShapeableImageView = itemView.findViewById(R.id.profileImage)
        val username: TextView = itemView.findViewById(R.id.user_username)
        val reviewedOn: TextView = itemView.findViewById(R.id.reviewedOn)
        val reviewText: TextView = itemView.findViewById(R.id.reviewText)
        val divider: View = itemView.findViewById(R.id.divider)

        val star2: ImageView = itemView.findViewById(R.id.star2)
        val star3: ImageView = itemView.findViewById(R.id.star3)
        val star4: ImageView = itemView.findViewById(R.id.star4)
        val star5: ImageView = itemView.findViewById(R.id.star5)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.merchant_reviews, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentReview = reviewList[position]
        profileRepository.isUserOrMerchant(currentReview.userUid){ result ->
            when(result){
                UserType.USER -> {
                    profileRepository.getUserData(currentReview.userUid) { result ->
                        val user = result.userData

                        holder.username.text = result.userData!!.username
                        Glide.with(holder.itemView.context)
                            .load(user!!.imageUrl)
                            .placeholder(R.drawable.anonymous)
                            .error(R.drawable.anonymous)
                            .centerCrop()
                            .into(holder.profileImage)
                    }
                }
                UserType.MERCHANT -> {
                    profileRepository.getMerchantData(currentReview.userUid) { result ->
                        val merchant = result.merchantData

                        holder.username.text = result.merchantData!!.merchantName
                        Glide.with(holder.itemView.context)
                            .load(merchant!!.profileImageUrl)
                            .placeholder(R.drawable.anonymous)
                            .error(R.drawable.anonymous)
                            .centerCrop()
                            .into(holder.profileImage)
                    }
                }
                else -> {}
            }

            when(currentReview.stars){
                4 -> {
                    holder.star5.visibility = View.GONE
                }
                3 -> {
                    holder.star5.visibility = View.GONE
                    holder.star4.visibility = View.GONE
                }
                2 -> {
                    holder.star5.visibility = View.GONE
                    holder.star4.visibility = View.GONE
                    holder.star3.visibility = View.GONE
                }
                1 -> {
                    holder.star5.visibility = View.GONE
                    holder.star4.visibility = View.GONE
                    holder.star3.visibility = View.GONE
                    holder.star2.visibility = View.GONE
                }
            }

            holder.reviewedOn.text = formatTimestamp(currentReview.reviewOn)
            holder.reviewText.setText(currentReview.review)
            holder.divider.visibility = if(position == reviewList.size) View.GONE else View.VISIBLE

        }
    }

    override fun getItemCount(): Int {
        return reviewList.size
    }

    fun updateData(newReviewList: List<MerchantReviews>) {
        reviewList = newReviewList
        notifyDataSetChanged()
    }

    fun formatTimestamp(timestamp: com.google.firebase.Timestamp): String {
        val sdf = SimpleDateFormat("dd MMM 'at' hh:mm a", Locale.getDefault())
        val date = timestamp.toDate()
        return sdf.format(date)
    }
}