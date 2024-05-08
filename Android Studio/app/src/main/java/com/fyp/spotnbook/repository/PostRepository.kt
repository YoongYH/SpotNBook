package com.fyp.spotnbook.repository

import com.fyp.spotnbook.model.Comment
import com.fyp.spotnbook.model.Post
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException

enum class DisplayType{
    RECOMMEND, FOLLOWING, USER_LIKED, USER_POST, TAGGED_MERCHANT
}

data class AddPostResult(val isSuccess: Boolean, val errorMessage: String? = null)
data class GetPostDataResult(val isSuccess: Boolean, val errorMessage: String? = null, val postData: Post?)
data class UpdatePostDataResult(val isSuccess: Boolean, val errorMessage: String? = null)
data class DeletePostResult(val isSuccess: Boolean, val errorMessage: String? = null)

class PostRepository {
    private val postsCollection: CollectionReference by lazy { FirebaseFirestore.getInstance().collection("posts") }

    //Get List of Post
    fun getPostList(onDataChanged: (List<Post>) -> Unit) {
        postsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                error.printStackTrace()
                return@addSnapshotListener
            }

            if (snapshot == null || snapshot.isEmpty) {
                return@addSnapshotListener
            }

            val postList = mutableListOf<Post>()

            for (document in snapshot.documents) {
                val postData = document.toObject(Post::class.java)
                postData?.postId = document.id
                postData?.let {
                    postList.add(it)
                }
            }
            onDataChanged(postList)
        }
    }


    //Get the Specific Post Data by postID
    fun getPostData(postId: String, onDataChanged: (GetPostDataResult) -> Unit) {
        postsCollection.document(postId).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val postData = documentSnapshot.toObject(Post::class.java)
                    onDataChanged(GetPostDataResult(true, null, postData))
                } else {
                    onDataChanged(GetPostDataResult(false, "Post is not Exist.", null))
                }
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
                onDataChanged(GetPostDataResult(false, exception.message, null))
            }
    }

    //Add new Post
    fun addPostData(newPost: Post, onDataChanged: (AddPostResult) -> Unit) {
        val newPostDocument = postsCollection.document()
        val postId = newPostDocument.id

        newPost.postId = postId

        newPostDocument.set(newPost)
            .addOnSuccessListener {
                onDataChanged(AddPostResult(true))
            }
            .addOnFailureListener { exception ->
                val errorCode = when (exception) {
                    is FirebaseFirestoreException -> exception.code.name
                    else -> "UNKNOWN_ERROR"
                }
                onDataChanged(AddPostResult(false, errorCode))
            }
    }

    //Update Post Data with Specific PostID
    fun updatePostData(postId: String, updatedPostData: Post, onDataChanged: (UpdatePostDataResult) -> Unit) {
        val postDocument = postsCollection.document(postId)

        postDocument.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    postDocument.set(updatedPostData)
                        .addOnSuccessListener {
                            onDataChanged(UpdatePostDataResult(true))
                        }
                        .addOnFailureListener { exception ->
                            val errorCode = when (exception) {
                                is FirebaseFirestoreException -> exception.code.name
                                else -> "UNKNOWN_ERROR"
                            }
                            onDataChanged(UpdatePostDataResult(false, errorCode))
                        }
                } else {
                    onDataChanged(UpdatePostDataResult(false, "POST_NOT_FOUND"))
                }
            }
            .addOnFailureListener { exception ->
                val errorCode = when (exception) {
                    is FirebaseFirestoreException -> exception.code.name
                    else -> "UNKNOWN_ERROR"
                }
                onDataChanged(UpdatePostDataResult(false, errorCode))
            }
    }

    //Delete Post Data with Specific PostID
    fun deletePost(postId: String, onDataChanged: (DeletePostResult) -> Unit) {
        val postDocument = postsCollection.document(postId)

        postDocument.delete()
            .addOnSuccessListener {
                onDataChanged(DeletePostResult(true))
            }
            .addOnFailureListener { exception ->
                val errorCode = when (exception) {
                    is FirebaseFirestoreException -> exception.code.name
                    else -> "UNKNOWN_ERROR"
                }
                onDataChanged(DeletePostResult(false, errorCode))
            }
    }

    //----------------------------------
    // Function to add a comment to a post

    fun addCommentToPost(
        postId: String,
        comment: Comment,
        onDataChanged: (Boolean, String?) -> Unit
    ) {
        val postDocument =
            postsCollection
                .document(postId)

        postDocument
            .update("comments", FieldValue.arrayUnion(comment))

            .addOnSuccessListener{

                onDataChanged(true, null)

            }

            .addOnFailureListener {
                    exception->

                onDataChanged(false, exception.message)
            }
    }

// Function to get comments for a post

    fun getCommentsForPost(postId : String, onDataChanged : (List<Comment>)->Unit) {
        postsCollection.document(postId)
            .get()

            .addOnSuccessListener {
                    documentSnapshot->

                if (documentSnapshot.exists()) {
                    val postData = documentSnapshot.toObject(Post::class.java)

                    val comments =
                        postData ?.comments
                            ?: emptyList()

                    onDataChanged(comments)
                }
                else {
                    onDataChanged(emptyList())
                }
            }

            .addOnFailureListener {
                    exception
                ->

                exception.printStackTrace()

                onDataChanged(emptyList())
            }
    }

    fun likePost(postId : String) {
        val currentUser = AuthenticationRepository().currentUser()

        postsCollection.document(postId)
            .get()
            .addOnSuccessListener {
                    documentSnapshot->
                if (documentSnapshot.exists()) {
                    val likedBy = documentSnapshot["likedBy"] as MutableList<String>

                    if (likedBy.contains(currentUser)) {
                        likedBy.remove(currentUser)
                    }
                    else {
                        likedBy.add(currentUser)
                    }

                    postsCollection.document(postId)
                        .update("likedBy", likedBy)
                }
            }
    }
}