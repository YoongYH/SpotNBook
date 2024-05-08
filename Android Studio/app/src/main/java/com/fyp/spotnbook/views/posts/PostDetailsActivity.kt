package com.fyp.spotnbook.views.posts

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.models.SlideModel
import com.fyp.spotnbook.R
import com.fyp.spotnbook.adapter.CommentsAdapter
import com.fyp.spotnbook.databinding.ActivityPostDetailsBinding
import com.fyp.spotnbook.model.Comment
import com.fyp.spotnbook.model.Post
import com.fyp.spotnbook.viewmodel.AuthenticationViewModel
import com.fyp.spotnbook.viewmodel.PostViewModel
import com.fyp.spotnbook.viewmodel.ProfileViewModel
import com.fyp.spotnbook.views.profile.ViewProfileActivity
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

// Activity to display details of a post
class PostDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPostDetailsBinding
    private lateinit var postViewModel: PostViewModel
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var authViewModel: AuthenticationViewModel

    private lateinit var btnBack: ImageView
    private lateinit var postCaption: TextView
    private lateinit var postBody: TextView
    private lateinit var postOn: TextView
    private lateinit var postAttachment: ImageSlider
    private var slider: MutableList<SlideModel> = mutableListOf() //Attachment List

    private lateinit var postData: Post

    private lateinit var taggedMerchantContainer: LinearLayout
    private lateinit var merchantName: TextView
    private lateinit var merchantAddress: TextView
    private lateinit var merchantRating: TextView
    private lateinit var merchantImage: ShapeableImageView

    private lateinit var authorInfo: LinearLayout
    private lateinit var profilePicture: ShapeableImageView
    private lateinit var authorName: TextView
    private lateinit var auth: FirebaseAuth


    private lateinit var commentsAdapter: CommentsAdapter
    private lateinit var commentsRecyclerView: RecyclerView
    private var comments: List<Comment> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the activity content using DataBindingUtil for easier view binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_post_details)
        // Initialize view models
        postViewModel = ViewModelProvider(this)[PostViewModel::class.java]
        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        authViewModel = ViewModelProvider(this)[AuthenticationViewModel::class.java]
        // Initialize commentsRecyclerView and commentsAdapter
        commentsRecyclerView = binding.commentsRecyclerView
        commentsAdapter = CommentsAdapter(comments)

        // Set the adapter for commentsRecyclerView
        commentsRecyclerView.adapter = commentsAdapter

        binding.moreOptions.setOnClickListener { view ->
            showPopupMenu(view)
        }

        // Firebase Authentication
        auth = FirebaseAuth.getInstance()


        // Initialize views from the layout
        binding.apply {
            this@PostDetailsActivity.btnBack = btnBack
            this@PostDetailsActivity.postCaption = postCaption
            this@PostDetailsActivity.postOn = postOn
            this@PostDetailsActivity.postAttachment = postAttachment
            this@PostDetailsActivity.postBody = postBody

            this@PostDetailsActivity.taggedMerchantContainer = taggedMerchant
            this@PostDetailsActivity.merchantName = merchantName
            this@PostDetailsActivity.merchantAddress = merchantAddress
            this@PostDetailsActivity.merchantRating = merchantRating
            this@PostDetailsActivity.merchantImage = merchantImage

            this@PostDetailsActivity.authorInfo = authorInfo
            this@PostDetailsActivity.profilePicture = profilePicture
            this@PostDetailsActivity.authorName = authorName
        }

        // Get the post ID from the intent
        val postId = intent.getStringExtra("postId")!!

        // Retrieve post data from ViewModel based on the postId from the intent
        if (!postId.isNullOrEmpty()) {
            postViewModel.getPostData(postId)
            postViewModel.getCommentsForPost(postId)
        }

        // Observe changes in post data
        postViewModel.getPostDataResult.observe(this) { result ->
            if (result.isSuccess) {
                postData = result.postData!!

                val isLiked = postData.likedBy.contains(authViewModel.currentUser())
                val likeImageResource =
                    if (isLiked) R.drawable.icon_like_clicked else R.drawable.icon_like
                binding.postBottomBarLike.setImageResource(likeImageResource)

                //Validation check If current user == owner of the post, moreOptions (three dot) icon will be displayed
                binding.moreOptions.visibility =
                    if (authViewModel.currentUser() != postData.postBy) View.GONE else View.VISIBLE

                // Populate UI with post data
                postCaption.text = postData.postCaption
                postBody.text = postData.postBody
                postOn.text = formatTimestamp(postData.postOn)

                //Map Attachment List into ImageView
                val attachmentList = postData.attachments
                for (stringValue in attachmentList) {
                    slider.add(SlideModel(stringValue))
                }
                postAttachment.setImageList(slider.toList())

                // Populate tagged merchant data if available
                if (postData.taggedMerchant.isBlank()) {
                    taggedMerchantContainer.visibility = View.GONE
                } else {
                    profileViewModel.getMerchantData(postData.taggedMerchant)
                    profileViewModel.merchantData.observe(this) { merchant ->
                        merchantName.text = merchant.merchantName
                        merchantAddress.text = merchant.address

                        //Calculate Average
                        val totalReviews = merchant.reviews.size
                        var totalStars = 0

                        for (review in merchant.reviews) {
                            totalStars += review.stars
                        }

                        val averageRatingScore = if (totalReviews > 0) {
                            totalStars.toDouble() / totalReviews
                        } else {
                            0.0
                        }
                        merchantRating.text = if (averageRatingScore == 0.0) "-" else String.format(
                            "%.1f",
                            averageRatingScore
                        )

                        Glide.with(this)
                            .load(merchant.profileImageUrl)
                            .centerCrop()
                            .placeholder(R.drawable.anonymous)
                            .error(R.drawable.anonymous)
                            .into(merchantImage)
                    }
                }

                // Populate author data
                profileViewModel.getUserData(postData.postBy)
                profileViewModel.userData.observe(this) { user ->
                    authorName.text = user.username

                    Glide.with(this)
                        .load(user.imageUrl)
                        .centerCrop()
                        .placeholder(R.drawable.anonymous)
                        .error(R.drawable.anonymous)
                        .into(profilePicture)
                }

                // Set click listeners for merchant and author info
                taggedMerchantContainer.setOnClickListener { merchantOnClickEvent(postData.taggedMerchant) }
                authorInfo.setOnClickListener { authorOnClickEvent(postData.postBy) }
            } else {
                Toast.makeText(this, "Error getting Post Data.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        postViewModel.deletePostResult.observe(this){result ->
            if(result.isSuccess){
                Toast.makeText(this, "Post successfully deleted", Toast.LENGTH_SHORT).show()
                finish()
            }
            else{
                Toast.makeText(this,result.errorMessage,Toast.LENGTH_SHORT).show()
            }
        }



        // Back button click event
        btnBack.setOnClickListener { finish() }


        postViewModel.getCommentsForPost(postId!!)
        // Observe comments data

        postViewModel.commentsLiveData.observe(this, Observer { comments ->
            // Update the UI with the comments data
            commentsAdapter.updateComments(comments)
        })




        // --- Click Listeners for Like & Comment Button --- //

        // Set click listener for post like button
        binding.postBottomBarLike.setOnClickListener {
            postViewModel.likePost(postId)
        }

        // Set click listener for post comment button
        binding.postBottomBarComment.setOnClickListener { postCommentOnClickEvent(postId?:"") }


        /*        // Enable zoom for the image slider
                postAttachment.startSliding(3000)
                postAttachment.enableImageSliderZoom(true)
        */
        // Get the post data
        postViewModel.getPostData(postId)


    }

    // --- Comments --- //
    // Handle post comment click event
    private fun postCommentOnClickEvent(postId: String) {
        // Show the comment dialog
        showCommentDialog(postId, auth)
    }

    // Function to show the comment dialog
    private fun showCommentDialog(postId: String, auth: FirebaseAuth) {
        val commentInput = EditText(this)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Add a Comment")
            .setView(commentInput)
            .setPositiveButton("Submit") { dialog, _ ->
                val commentText = commentInput.text.toString()
                // Retrieve the user's image URL from Firestore based on their user ID
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val userId = currentUser.uid
                    val firestore = FirebaseFirestore.getInstance()
                    firestore.collection("users").document(userId).get()
                        .addOnSuccessListener { documentSnapshot ->
                            if (documentSnapshot.exists()) {
                                val userImageUrl = documentSnapshot.getString("imageUrl")
                                val newComment = Comment(
                                    uid = "comment_${UUID.randomUUID()}",
                                    comment = commentText,
                                    commentedOn = Timestamp.now(),
                                    userImage = userImageUrl ?: ""
                                )
                                postViewModel.addComment(postId, newComment)
                            } else {
                                // Handle case where user document does not exist
                            }
                        }
                        .addOnFailureListener { exception ->
                            // Handle any errors
                        }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()

        // Show keyboard when the dialog is shown
        commentInput.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }


    // Format timestamp into a readable date format
    fun formatTimestamp(timestamp: Timestamp): String {
        val sdf = SimpleDateFormat("dd MMM 'at' hh:mm a", Locale.getDefault())
        val date = timestamp.toDate()
        return sdf.format(date)
    }

    private fun merchantOnClickEvent(merchantUid: String){
        val intent = Intent(this, ViewProfileActivity::class.java)
        intent.putExtra("uid", merchantUid)
        startActivity(intent)
    }

    private fun authorOnClickEvent(authorUid: String){
        val intent = Intent(this, ViewProfileActivity::class.java)
        intent.putExtra("uid", authorUid)
        startActivity(intent)
    }


    private fun showDeleteConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Post")
            .setMessage("Are you sure you want to delete this post?")
            .setPositiveButton("Delete") { _, _ ->
                val currentUser = auth.currentUser
                if (currentUser != null && currentUser.uid == postData.postBy) {
                    val firestore = FirebaseFirestore.getInstance()
                    firestore.collection("users").document(currentUser.uid).get()
                        .addOnSuccessListener { documentSnapshot ->
                            if (documentSnapshot.exists()) {
                                // Delete the post
                                postViewModel.deletePost(postData.postId)
                            } else {
                                // Handle case where user document does not exist
                            }
                        }
                        .addOnFailureListener { exception ->
                            // Handle any errors
                        }
                } else {
                    Toast.makeText(this, "You can only delete your own posts", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Function to show the dropdown menu
    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.post_options_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
/*                R.id.menu_item_edit -> {
                    // Handle edit action
                    true
                }*/
                R.id.menu_item_delete -> {
                    // Handle delete action
                    showDeleteConfirmationDialog()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }



    // -----ViewPager2 ---- //
    // Adapter class for the ViewPager2 to display images in a slideshow
    inner class ImageSliderAdapter(private val imageUrls: List<String>) :
        RecyclerView.Adapter<ImageSliderAdapter.ViewHolder>() {

        // ViewHolder class to hold the views for each item in the ViewPager2
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imageView: ImageView = itemView.findViewById(R.id.imageView)
        }

        // onCreateViewHolder is called when the RecyclerView needs a new ViewHolder to represent an item
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            // Inflate the item_image_slider layout file to create a new item view
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image_slider, parent, false)
            // Return a new ViewHolder instance with the inflated view
            return ViewHolder(view)
        }

        // onBindViewHolder is called to bind the data to the views in each ViewHolder
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            // Use Glide to load the image at the current position in the imageUrls list into the imageView
            Glide.with(holder.itemView)
                .load(imageUrls[position])
                .into(holder.imageView)
        }

        // getItemCount returns the total number of items in the data set
        override fun getItemCount(): Int {
            return imageUrls.size
        }
    }

}

