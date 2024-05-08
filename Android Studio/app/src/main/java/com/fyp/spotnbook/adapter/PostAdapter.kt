package com.fyp.spotnbook.adapter

import android.annotation.SuppressLint
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
import com.fyp.spotnbook.model.Post
import com.fyp.spotnbook.repository.AuthenticationRepository
import com.fyp.spotnbook.repository.DisplayType
import com.fyp.spotnbook.repository.PostRepository
import com.fyp.spotnbook.repository.ProfileRepository
import com.fyp.spotnbook.views.posts.PostDetailsActivity
import com.google.android.material.imageview.ShapeableImageView

class PostAdapter(private val displayType: DisplayType, private val targetUserId: String) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {
    private var profileRepository = ProfileRepository()
    private var postRepository = PostRepository()
    private var auth = AuthenticationRepository()
    private var postList: MutableList<Post> = mutableListOf()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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
        val post = postList[position]
        profileRepository.getFollowingList { followingList ->
            val filteredList = when (displayType) {
                DisplayType.RECOMMEND -> postList
                DisplayType.FOLLOWING -> postList.filter { followingList.contains(it.postBy) }
                DisplayType.USER_LIKED -> postList.filter { targetUserId in it.likedBy }
                DisplayType.USER_POST -> postList.filter { targetUserId == it.postBy }
                DisplayType.TAGGED_MERCHANT -> {
                    postList.filter { targetUserId == it.taggedMerchant }
                }
            }

            val isLastItem = position == filteredList.size - 1
            val isPostInFilteredList = filteredList.contains(post)

            if (isPostInFilteredList) {
                displayPost(holder, post)
                holder.itemView.visibility = View.VISIBLE

                val params = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
                if (isLastItem && filteredList.size >= 3) {
                    params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, 255) // Set bottom margin for the last item
                }
                holder.itemView.layoutParams = params
            } else {
                holder.itemView.visibility = View.GONE
            }
        }
    }

    private fun displayPost(holder: ViewHolder, post: Post) {
        // Set initial like button icon based on the likedBy list
        val isLiked = post.likedBy.contains(auth.currentUser())
        val likeImageResource = if (isLiked) R.drawable.icon_like_clicked else R.drawable.icon_like
        holder.btnLike.setImageResource(likeImageResource)

        //Author (User) Data
        val postBy = post.postBy
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
        val taggedMerchant = post.taggedMerchant
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

        holder.postCaption.text = post.postCaption

        //Post Thumbnail
        val firstAttachmentUrl = post.attachments.firstOrNull()
        Glide.with(holder.itemView.context)
            .load(firstAttachmentUrl)
            .centerCrop()
            .into(holder.postThumbnail)

        //Click on the Post
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, PostDetailsActivity::class.java)
            intent.putExtra("postId", post.postId)
            holder.itemView.context.startActivity(intent)
        }

        //---Waiting to add from brendan---
        holder.btnLike.setOnClickListener {
            postRepository.likePost(post.postId)
        }
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: MutableList<Post>?) {
        newData?.let {
            postList = it
            notifyDataSetChanged()
        }
    }
}