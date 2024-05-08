package com.fyp.spotnbook.model

import com.google.firebase.Timestamp

data class Post(
    var postId: String = "",
    var postCaption: String = "",
    var postBody: String = "",
    var postBy: String = "",
    var postOn: Timestamp = Timestamp(0, 0),
    var taggedMerchant: String = "",
    var attachments: ArrayList<String> = ArrayList(),
    var likedBy: ArrayList<String> = ArrayList(),
    var comments: ArrayList<Comment> = ArrayList()
)

// Data class representing a comment
data class Comment(
    var uid: String = "", // Unique identifier for the comment
    var comment: String = "", // Content of the comment
    var commentedOn: Timestamp = Timestamp(0, 0) ,// Timestamp when the comment was created
    var userImage: String = "" //Drawable resource for user's image
)