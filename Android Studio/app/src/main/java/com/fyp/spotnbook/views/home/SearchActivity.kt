package com.fyp.spotnbook.views.home

import SearchUserResultAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fyp.spotnbook.R
import com.fyp.spotnbook.adapter.SearchPostResultAdapter
import com.fyp.spotnbook.databinding.ActivitySearchBinding
import com.google.android.material.card.MaterialCardView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException


class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding

    private lateinit var btnBack: ImageView
    private lateinit var searchBar: MaterialCardView
    private lateinit var searchText: EditText
    private lateinit var btnClear: ImageView

    //User Result Section
    private lateinit var textNoUser: TextView
    private lateinit var userResultLayout: LinearLayout
    private lateinit var userRecyclerView: RecyclerView

    //Post Result Section
    private lateinit var textNoPost: TextView
    private lateinit var postResultLayout: LinearLayout
    private lateinit var postRecyclerView: RecyclerView

    private lateinit var user_adapter: SearchUserResultAdapter
    private lateinit var post_adapter: SearchPostResultAdapter
    private var usersList: MutableList<String> = mutableListOf()
    private var postsList: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search)

        binding.apply {
            this@SearchActivity.btnBack = btnBack
            searchBar = searchBarCardView
            searchText = searchBarEdittext
            this@SearchActivity.btnClear = btnClear

            //User Result Section
            textNoUser = textNoUserFound
            this@SearchActivity.userResultLayout = userResultLayout
            this@SearchActivity.userRecyclerView = userRecyclerView

            //Post Result Section
            textNoPost = textNoPostFound
            this@SearchActivity.postResultLayout = postResultLayout
            this@SearchActivity.postRecyclerView = postRecyclerView
        }

        //----- Initial -----
        searchText.requestFocus()
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(searchText, InputMethodManager.SHOW_IMPLICIT)

        val userLayoutManager = LinearLayoutManager(this)
        user_adapter = SearchUserResultAdapter(this@SearchActivity, usersList)
        userRecyclerView.adapter = user_adapter
        userRecyclerView.layoutManager = userLayoutManager

        val postLayoutManager = LinearLayoutManager(this)
        post_adapter = SearchPostResultAdapter(this@SearchActivity, postsList)
        postRecyclerView.adapter = post_adapter
        postRecyclerView.layoutManager = postLayoutManager

        //----- Listeners for UI Components -----
        // Search - Text Change Listener
        searchText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // This method is called before the text is changed.
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // This method is called when the text is changed.
            }

            override fun afterTextChanged(s: Editable) {
                btnClear.visibility = if (!searchText.text.isNullOrBlank()) View.VISIBLE else View.GONE
            }
        })

        // Search - Focus Listener
        searchText.setOnFocusChangeListener { _, hasFocus ->
            // Change stroke Color and stroke Width when search bar is focused
            searchBar.strokeColor = if (hasFocus) resources.getColor(R.color.primary, null) else resources.getColor(R.color.black, null)
            searchBar.strokeWidth = if (hasFocus) 4 else 2

            // Not allowing empty search
            if (!hasFocus) {
                if (searchText.text.isEmpty()) {
                    searchText.requestFocus()
                }
            }
        }

        // Search - Editor Action
        searchText.setOnEditorActionListener { _, actionId, event ->
            if ((actionId == EditorInfo.IME_ACTION_DONE ||
                        (event != null && event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER))
                && searchText.text.isNotBlank() // Check if EditText is not empty
            ) {
                searchText.clearFocus()
                sendSearchRequest(searchText.text.toString())
                true
            } else {
                false
            }
        }

        // Button Events
        btnBack.setOnClickListener { finish() }
        btnClear.setOnClickListener { handleClearButton(searchText) }
    }

    private fun handleClearButton(searchText: EditText){
        if(!searchText.text.isNullOrBlank()){
            searchText.setText("")
            searchText.requestFocus()

            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(searchText, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun sendSearchRequest(keyword: String) {
        val client = OkHttpClient()

        // Create Request
        val json = JSONObject()
        json.put("keyword", keyword.lowercase())
        val requestBody =
            RequestBody.create("application/json".toMediaTypeOrNull(), json.toString())

        val request = Request.Builder()
            .url("http://192.168.1.42:5000/search")
            .post(requestBody)
            .build()

        //Send Request
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let {
                    try {
                        val responseData = it.string()
                        val jsonObject = JSONObject(responseData)

                        val usersArray = jsonObject.getJSONArray("users")
                        val usersList_response = mutableListOf<String>()

                        val postsArray = jsonObject.getJSONArray("posts")
                        val postsList_response = mutableListOf<String>()

                        if (usersArray.length() != 0){
                            for (i in 0 until usersArray.length()) {
                                val userArray = usersArray.getJSONArray(i)
                                val similarity = userArray.getString(1).toDouble()
                                val userId = userArray.getString(0)

                                if (similarity > 0.5 || (usersList_response.isEmpty() && similarity > 0.2)) {
                                    usersList_response.add(userId)
                                }
                            }
                        }

                        if (postsArray.length() != 0){
                            for (i in 0 until postsArray.length()) {
                                val postArray = postsArray.getJSONArray(i)
                                val similarity = postArray.getString(2).toDouble()

                                if(similarity > 0){
                                    val postId = postArray.getString(0)
                                    postsList_response.add(postId)
                                }
                            }
                        }

                        runOnUiThread {
                            if (usersList_response != usersList){
                                usersList = usersList_response
                                user_adapter.updateData(usersList)
                            }

                            textNoUser.visibility = if (usersList.isEmpty()) View.VISIBLE else View.GONE
                            userResultLayout.visibility = if (usersList.isEmpty()) View.GONE else View.VISIBLE

                            if (postsList_response != postsList){
                                postsList = postsList_response
                                post_adapter.updateData(postsList)
                            }

                            textNoPost.visibility = if (postsList.isEmpty()) View.VISIBLE else View.GONE
                            postResultLayout.visibility = if (postsList.isEmpty()) View.GONE else View.VISIBLE
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
        })
    }
}