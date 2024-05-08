package com.fyp.spotnbook.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fyp.spotnbook.R
import com.fyp.spotnbook.repository.PostRepository
import com.fyp.spotnbook.repository.ProfileRepository
import com.fyp.spotnbook.views.posts.PostDetailsActivity
import com.google.android.material.imageview.ShapeableImageView

class SearchPostResultAdapter(private val context: Context, private var dataList: MutableList<String>) :
    RecyclerView.Adapter<SearchPostResultAdapter.ViewHolder>() {
    private val postRepository = PostRepository()
    private val profileRepository = ProfileRepository()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val author: TextView = itemView.findViewById(R.id.authorName)
        val postCaption: TextView = itemView.findViewById(R.id.postCaption)
        val location: TextView = itemView.findViewById(R.id.location)
        val postThumbnail: ShapeableImageView = itemView.findViewById(R.id.postAttachment)
        val profilePicture: ShapeableImageView = itemView.findViewById(R.id.profilePicture)
        val btnLike: ImageView = itemView.findViewById(R.id.likeButton)
        val locationContainer: LinearLayout = itemView.findViewById(R.id.locationContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.post_design, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val postId = dataList[position]

        postRepository.getPostData(postId) { result ->
            if (result.isSuccess) {
                val postData = result.postData!!

                //Author (User) Data
                val postBy = postData.postBy
                if (postBy.isNotBlank()) {
                    profileRepository.getUserData(postBy) { user ->
                        user.userData?.let { userData ->
                            holder.author.text = userData.username

                            Glide.with(holder.itemView.context)
                                .load(userData.imageUrl)
                                .placeholder(R.drawable.anonymous)
                                .error(R.drawable.anonymous)
                                .centerCrop()
                                .into(holder.profilePicture)
                        } ?: run {
                            holder.author.text = "Unknown Author"
                        }
                    }
                } else {
                    holder.author.text = "Unknown Author"
                }

                //Merchant Data
                val taggedMerchant = postData.taggedMerchant
                if (taggedMerchant.isNotBlank()) {
                    profileRepository.getMerchantData(taggedMerchant) { merchant ->
                        merchant.merchantData?.let { merchantData ->
                            holder.location.text = merchantData.merchantName
                            holder.locationContainer.visibility = View.VISIBLE
                        } ?: run {
                            holder.location.text = "Merchant not Found"
                            holder.locationContainer.visibility = View.VISIBLE
                        }
                    }
                } else {
                    holder.locationContainer.visibility = View.GONE // Set to GONE here
                }

                holder.postCaption.text = postData.postCaption

                //Post Thumbnail
                val firstAttachmentUrl = postData.attachments.firstOrNull()
                Glide.with(context)
                    .load(firstAttachmentUrl)
                    .placeholder(R.drawable.anonymous)
                    .error(R.drawable.anonymous)
                    .centerCrop()
                    .into(holder.postThumbnail)

                //Click on the Post
                holder.itemView.setOnClickListener {
                    val intent = Intent(holder.itemView.context, PostDetailsActivity::class.java)
                    intent.putExtra("postId", postData.postId)
                    holder.itemView.context.startActivity(intent)
                }

                holder.btnLike.setOnClickListener {

                }
            }

            val params = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
            if (position == dataList.size - 1) {
                params.setMargins(
                    params.leftMargin,
                    params.topMargin,
                    params.rightMargin,
                    255
                )
            } else {
                params.setMargins(
                    params.leftMargin,
                    params.topMargin,
                    params.rightMargin,
                    0
                )
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return dataList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<String>) {
        dataList = newData.toMutableList()
        notifyDataSetChanged()
    }
}