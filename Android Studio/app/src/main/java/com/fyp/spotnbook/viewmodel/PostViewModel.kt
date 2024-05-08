package com.fyp.spotnbook.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fyp.spotnbook.model.Comment
import com.fyp.spotnbook.model.Merchant
import com.fyp.spotnbook.model.Post
import com.fyp.spotnbook.repository.AddPostResult
import com.fyp.spotnbook.repository.DeletePostResult
import com.fyp.spotnbook.repository.GetPostDataResult
import com.fyp.spotnbook.repository.PostRepository
import com.fyp.spotnbook.repository.ProfileRepository
import com.fyp.spotnbook.repository.UpdatePostDataResult

class PostViewModel() : ViewModel() {
    private val postRepository = PostRepository()

    //----------Live Data----------
    private val _postList = MutableLiveData<List<Post>>()
    val postList: LiveData<List<Post>> get() = _postList

    private val _addPostResult = MutableLiveData<AddPostResult>()
    val addPostResult: LiveData<AddPostResult> get() = _addPostResult

    private val _getPostDataResult = MutableLiveData<GetPostDataResult>()
    val getPostDataResult: LiveData<GetPostDataResult> get() = _getPostDataResult

    private val _updatePostDataResult = MutableLiveData<UpdatePostDataResult>()
    val updatePostDataResult: LiveData<UpdatePostDataResult> get() = _updatePostDataResult

    private val _deletePostResult = MutableLiveData<DeletePostResult>()
    val deletePostResult: LiveData<DeletePostResult> get() = _deletePostResult

    //-----------------------------------------------------------------------------------------------------------
    // LiveData for holding the current post data
    private val _postData = MutableLiveData<Post>()
    val postData: LiveData<Post> get() = _postData

    // LiveData for holding the list of comments for a post
    private val _commentsLiveData = MutableLiveData<List<Comment>>()
    val commentsLiveData: LiveData<List<Comment>> get() = _commentsLiveData
    //-----------------------------------------------------------------------------------------------------------

    //----------VM Methods----------
    init {
        getPostList()
    }

    private fun getPostList() {
        postRepository.getPostList { postList ->
            _postList.postValue(postList)
        }
    }

    fun getPostData(postID: String) {
        postRepository.getPostData(postID) { result ->
            _getPostDataResult.postValue(result)
        }
    }

    fun addPost(newPost: Post){
        postRepository.addPostData(newPost){ result ->
            _addPostResult.postValue(result)
        }
    }

    fun updatePostData(updatedPost: Post){
        postRepository.updatePostData(_getPostDataResult.value!!.postData!!.postId, updatedPost){ result ->
            _updatePostDataResult.postValue(result)
        }
    }

    fun deletePost(postID: String){
        postRepository.deletePost(postID){ result ->
            _deletePostResult.postValue(result)
        }
    }






    //-----------------------------------------------------------------------------------------------------------
    // Function to set the current post data
    fun setPostData(post: Post) {
        _postData.value = post
    }
    /*
    // Function to check if a post is liked by the user
     fun isPostLiked(postId: String): Boolean {
         val post = _postData.value ?: return false
         return post.likedBy.contains(postId)
     }*/
    // Function to toggle the like status of a post
    fun likePost(postId: String){
        postRepository.likePost(postId)
    }
    // LiveData for holding the list of merchants
    private val _merchantsList = MutableLiveData<List<Merchant>>()
    val merchantsList: LiveData<List<Merchant>> get() = _merchantsList
    // Function to fetch the list of merchants
    fun fetchMerchantsList() {
        val profileRepository = ProfileRepository()
        profileRepository.getMerchantsList { merchantsList ->
            _merchantsList.postValue(merchantsList)
        }
    }
    //Add Comment//
    fun addComment(postId: String, comment: Comment) {
        postRepository.addCommentToPost(postId, comment) { isSuccess, errorMessage ->
            if (isSuccess) {
                // Comment added successfully
                Log.d("PostViewModel", "Comment added successfully")
            } else {
                // Handle the failure, possibly showing an error message
                errorMessage?.let { error ->
                    Log.e("PostViewModel", "Failed to add comment: $error")
                }
            }
        }
    }
    //get Comments for a post //
    fun getCommentsForPost(postId: String) {
        postRepository.getCommentsForPost(postId) { comments ->
            _commentsLiveData.value = comments
        }
    }
    //-----------------------------------------------------------------------------------------------------------
}