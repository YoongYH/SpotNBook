package com.fyp.spotnbook.views.posts

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.fyp.spotnbook.R
import com.fyp.spotnbook.adapter.PostAdapter
import com.fyp.spotnbook.databinding.FragmentPostBinding
import com.fyp.spotnbook.model.Post
import com.fyp.spotnbook.repository.DisplayType
import com.fyp.spotnbook.viewmodel.AuthenticationViewModel
import com.fyp.spotnbook.viewmodel.PostViewModel
import com.fyp.spotnbook.viewmodel.ProfileViewModel
import com.google.rpc.context.AttributeContext.Auth
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

class PostFragment : Fragment() {
    private lateinit var binding: FragmentPostBinding
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var postViewModel: PostViewModel
    private lateinit var authViewModel: AuthenticationViewModel

    companion object {
        private const val ARG_DISPLAY_TYPE = "displayType"
        private const val ARG_UID = "uid"

        fun newInstance(displayType: DisplayType, uid: String): PostFragment {
            val fragment = PostFragment()
            val args = Bundle()
            args.putSerializable(ARG_DISPLAY_TYPE, displayType)
            args.putString(ARG_UID, uid)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPostBinding.bind(view)

        val displayType = arguments?.getSerializable(ARG_DISPLAY_TYPE) as DisplayType
        val uid = arguments?.getSerializable(ARG_UID) as String

        val adapter = PostAdapter(displayType, uid)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        postViewModel = ViewModelProvider(requireActivity())[PostViewModel::class.java]
        profileViewModel = ViewModelProvider(requireActivity())[ProfileViewModel::class.java]
        authViewModel = ViewModelProvider(requireActivity())[AuthenticationViewModel::class.java]

        //----------Observers----------
        postViewModel.postList.observe(viewLifecycleOwner) { newPostList ->
            val mutablePostList: MutableList<Post> = newPostList.toMutableList()
            adapter.updateData(mutablePostList)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_post, container, false)
    }
}