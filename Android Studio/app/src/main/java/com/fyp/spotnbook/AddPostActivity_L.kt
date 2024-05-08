/*
package com.fyp.spotnbook.views



import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.fyp.spotnbook.databinding.ActivityAddPostBinding
import com.fyp.spotnbook.model.Post
import com.fyp.spotnbook.repository.AuthenticationRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*

class AddPostActivity_L : AppCompatActivity() {

    private lateinit var binding: ActivityAddPostBinding
    private var mediaUri: Uri? = null

    // Firebase Firestore instance
    private val db = FirebaseFirestore.getInstance()

    //Firebase Storage
    private val storageReference: StorageReference = FirebaseStorage.getInstance().reference

    private val currentUserUid: String = AuthenticationRepository().currentUser()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set click listener for the image view to open the gallery
        binding.imagePost.setOnClickListener {
            openGallery()
        }

        // Set click listener for the save button to save the post
        binding.saveNewPostBtn.setOnClickListener {
            savePost()
        }

        // Set click listener for the close button to finish the activity
        binding.closeNewPostBtn.setOnClickListener {
            finish()
        }
    }

    // Function to open the gallery to select an image
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher.launch(intent)
    }

    // Activity result launcher to handle the result from opening the gallery
    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            mediaUri = data?.data
            // Set the selected image to ImageView
            binding.imagePost.setImageURI(mediaUri)
        }
    }

    // Function to save the post to Firebase Firestore
    private fun savePost() {
        val postCaption = binding.descriptionPost.text.toString()
        val postBody = binding.postBody.text.toString()
        val currentUserUid: String = AuthenticationRepository().currentUser()
        // Check if an image is selected and a caption is entered
        if (mediaUri != null && postCaption.isNotEmpty()) {
            // Generate a unique postId
            val postId = UUID.randomUUID().toString()
            // Upload the attachment to Firebase Storage
            uploadAttachmentToStorage(mediaUri!!) { attachmentUrl ->
                // Create a new Post object with the attachment URL
                val post = Post(
                    postCaption = postCaption,
                    postBody  = postBody,
                    postBy = currentUserUid,
                    postOn = Timestamp.now(),
                    attachments = arrayListOf(attachmentUrl)
                )
                // Add the post to Firestore
                addPostToFirestore(post)
            }
        } else {
            // Show a toast message if image or caption is missing
            Toast.makeText(this, "Please select an image and enter a description", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadAttachmentToStorage(uri: Uri, onComplete: (String) -> Unit) {
        val uid = currentUserUid
        val attachmentRef = storageReference.child("post_attachments/$uid/${UUID.randomUUID()}")
        attachmentRef.putFile(uri).addOnSuccessListener {
            attachmentRef.downloadUrl.addOnSuccessListener { downloadUri ->
                onComplete(downloadUri.toString())
            }
        }.addOnFailureListener { e ->
            Log.e("Error", "Failed to upload attachment")
            onComplete("") // Return empty string if upload fails
        }
    }


    // Function to add the post to Firebase Firestore
    private fun addPostToFirestore(post: Post) {
        db.collection("posts")
            .add(post)
            .addOnSuccessListener { documentReference ->
                // Update the postId in the Post object with the Firestore-generated ID
 */
/*               val postId = documentReference.id
                post.postId = postId

                // Update the Firestore document with the updated Post object
                db.collection("post")
                    .document(postId)
                    .set(post)
                    .addOnSuccessListener {
                        // Show a success message and finish the activity
                        Toast.makeText(this, "Post added successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        // Show an error message if updating the post fails
                        Log.e("Error", "Failed to update post with postId", e)
                        Toast.makeText(this, "Failed to update post with postId: ${e.message}", Toast.LENGTH_SHORT).show()
                    }

                    *//*

                Toast.makeText(this, "Post added successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                // Show an error message if adding the post fails
                Log.e("Error", "Failed to add post", e)
                Toast.makeText(this, "Failed to add post: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}
*/

package com.fyp.spotnbook.views

import com.fyp.spotnbook.R
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.fyp.spotnbook.ImageSliderAdapter
import com.fyp.spotnbook.databinding.ActivityAddPostBinding
import com.fyp.spotnbook.model.Post
import com.fyp.spotnbook.repository.AuthenticationRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*

class AddPostActivity_L : AppCompatActivity() {

    private lateinit var binding: ActivityAddPostBinding
    private var mediaUris: MutableList<Uri> = mutableListOf() // Store selected image URIs
    private lateinit var viewPager: ViewPager2
    private var isButtonClickable = true
    private var isSaveButtonClickable = true


    // Firebase Firestore instance
    private val db = FirebaseFirestore.getInstance()

    // Firebase Storage instance
    private val storageReference: StorageReference = FirebaseStorage.getInstance().reference

    private val currentUserUid: String = AuthenticationRepository().currentUser()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Clear the mediaUris list when the activity is created
        mediaUris.clear()

        // Set up the spinner
        setupSpinner()

        // Set click listener for the image view to open the gallery
        binding.btnChooseImage.setOnClickListener {
            openGallery()
        }

        // Set click listener for the save button to save the post
        binding.saveNewPostBtn.setOnClickListener {
            savePost()
            }


        // Set click listener for the close button to finish the activity
        binding.closeNewPostBtn.setOnClickListener {
            showLeaveConfirmationDialog()
        }
    }

    // Function to setup the spinner with merchant names
    private fun setupSpinner() {
        fetchMerchants()
    }

    // Function to open the gallery to select images
    private fun openGallery() {
        if (isButtonClickable) {
            isButtonClickable = false
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true) // Allow multiple image selection
            resultLauncher.launch(intent)
            Handler().postDelayed({
                isButtonClickable = true
            }, 2000) // Set the interval in milliseconds (e.g., 1000ms = 1 second)

            // Clear all selected images
            mediaUris.clear()

            // Update the ViewPager2 with an empty adapter
            val emptyAdapter = ImageSliderAdapter(emptyList(), binding.viewPager)
            binding.viewPager.adapter = emptyAdapter
        }
    }

    // Activity result launcher to handle the result from opening the gallery
    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            if (data?.clipData != null) {
                val clipData = data.clipData!!
                for (i in 0 until clipData.itemCount) {
                    val uri = clipData.getItemAt(i).uri
                    mediaUris.add(uri) // Add selected image URIs to the list
                }
            } else if (data?.data != null) {
                val uri = data.data!!
                mediaUris.add(uri) // Add selected image URI to the list
            }

            // Update the ViewPager2 with the selected images
            val adapter = ImageSliderAdapter(mediaUris.map { it.toString() }, binding.viewPager)
            binding.viewPager.adapter = adapter
        }
    }

    /*
    // Function to save the post to Firebase Firestore
    private fun savePost() {
        val postCaption = binding.descriptionPost.text.toString()
        val postBody = binding.postBody.text.toString()
        val currentUserUid: String = AuthenticationRepository().currentUser()
        // Check if images are selected and a caption is entered
        if (mediaUris.isNotEmpty() && postCaption.isNotEmpty()) {
            val attachmentUrls = mutableListOf<String>()
            // Upload each image to Firebase Storage
            mediaUris.forEachIndexed { index, uri ->
                uploadAttachmentToStorage(uri) { attachmentUrl ->
                    attachmentUrls.add(attachmentUrl)
                    // If all images are uploaded, create and add the post to Firestore
                    if (attachmentUrls.size == mediaUris.size) {
                        val post = Post(
                            postCaption = postCaption,
                            postBody = postBody,
                            postBy = currentUserUid,
                            postOn = Timestamp.now(),
                            attachments = ArrayList(attachmentUrls) // Convert MutableList to ArrayList
                        )
                        addPostToFirestore(post)
                    }
                }
            }
        } else {
            // Show a toast message if images or caption is missing
            Toast.makeText(this, "Please select images and enter a description", Toast.LENGTH_SHORT).show()
        }
    }
    */
    // Function to save the post to Firebase Firestore
    private fun savePost() {
        if (isFinishing) {
            // Activity is finishing, do not proceed with saving the post
            return
        }
        val postCaption = binding.descriptionPost.text.toString()
        val postBody = binding.postBody.text.toString()
        val currentUserUid: String = AuthenticationRepository().currentUser()
        // Check if images are selected and a caption is entered
        if (mediaUris.isNotEmpty() && postCaption.isNotEmpty()) {
            val attachmentUrls = mutableListOf<String>()
            // Upload each image to Firebase Storage
            mediaUris.forEachIndexed { index, uri ->
                uploadAttachmentToStorage(uri) { attachmentUrl ->
                    attachmentUrls.add(attachmentUrl)
                    // If all images are uploaded, create and add the post to Firestore
                    if (attachmentUrls.size == mediaUris.size) {
                        // Get the selected merchant ID from the spinner
                        val selectedMerchant = binding.spinnerMerchants.selectedItem as String
                        val db = FirebaseFirestore.getInstance()
                        db.collection("merchants")
                            .whereEqualTo("merchantName", selectedMerchant)
                            .get()
                            .addOnSuccessListener { result ->
                                if (!result.isEmpty) {
                                    val merchantId = result.documents[0].id
                                    val post = Post(
                                        postCaption = postCaption,
                                        postBody = postBody,
                                        postBy = currentUserUid,
                                        postOn = Timestamp.now(),
                                        attachments = ArrayList(attachmentUrls), // Convert MutableList to ArrayList
                                        taggedMerchant = merchantId
                                    )
                                    // Show confirmation dialog before adding the post
                                    showConfirmationDialog(post)
                                } else {
                                    Toast.makeText(this, "Failed to find merchant ID for $selectedMerchant", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("Error", "Failed to fetch merchant ID", e)
                                Toast.makeText(this, "Failed to fetch merchant ID: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }
        } else {
            // Show a toast message if images or caption is missing
            Toast.makeText(this, "Please select images and enter your captions", Toast.LENGTH_SHORT).show()
        }
        Handler().postDelayed({
            isSaveButtonClickable = true
        }, 1500) // Set the interval in milliseconds (e.g., 1000ms = 1 second)
    }

    // Confirmation dialog when clicking on the saveNewPostBtn //
    private fun showConfirmationDialog(post: Post) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm")
            .setMessage("Are you sure you want to add this post?")
            .setPositiveButton("Yes") { dialog, which ->
                addPostToFirestore(post)
                // Setup the ViewPager2 with the ImageSliderAdapter after adding the post
                val adapter = ImageSliderAdapter(post.attachments, binding.viewPager)
                binding.viewPager.adapter = adapter
            }
            .setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    //Confirmation Dialog when clicking the clicking the closeNewPostBtn
    private fun showLeaveConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm")
            .setMessage("Are you sure you want to leave this page?")
            .setPositiveButton("Yes") { dialog, which ->
                if (!isFinishing) {
                    finish()
                }
            }
            .setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }



    // Function to upload an image to Firebase Storage
    private fun uploadAttachmentToStorage(uri: Uri, onComplete: (String) -> Unit) {
        val uid = currentUserUid
        val attachmentRef = storageReference.child("post_attachments/$uid/${UUID.randomUUID()}")
        attachmentRef.putFile(uri).addOnSuccessListener {
            attachmentRef.downloadUrl.addOnSuccessListener { downloadUri ->
                onComplete(downloadUri.toString())
            }
        }.addOnFailureListener { e ->
            Log.e("Error", "Failed to upload attachment")
            onComplete("") // Return empty string if upload fails
        }
    }

    //When pressing your device's back button//
    override fun onBackPressed() {
        super.onBackPressed()
        showLeaveConfirmationDialog()
    }

    // Function to add the post to Firebase Firestore
    private fun addPostToFirestore(post: Post) {
        // Add the post to the "posts" collection in Firestore
        db.collection("posts")
            .add(post)
            .addOnSuccessListener { documentReference ->
                // When the post is successfully added, get the auto-generated postId
                val postId = documentReference.id
                // Update the postId in the Post object with the Firestore-generated ID
                post.postId = postId
                // Update the Firestore document with the updated Post object
                db.collection("posts")
                    .document(postId)
                    .set(post)
                    .addOnSuccessListener {
                        // Show a success message and finish the activity
                        Toast.makeText(this, "Post added successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        // Show an error message if updating the post fails
                        Log.e("Error", "Failed to update post with postId", e)
                        Toast.makeText(this, "Failed to update post with postId: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                // Show an error message if adding the post fails
                Log.e("Error", "Failed to add post", e)
                Toast.makeText(this, "Failed to add post: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    //Function to fetch Merchant data from collection in Firestore
    private fun fetchMerchants() {
        val db = FirebaseFirestore.getInstance()
        db.collection("merchants")
            .get()
            .addOnSuccessListener { result ->
                val merchantNames = result.documents.mapNotNull { it.getString("merchantName") }
                if (merchantNames.isNotEmpty()) {
                    // Update the spinner with the merchant names
                    val adapter = ArrayAdapter(this, R.layout.spinner_dropdown_item, merchantNames)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerMerchants.adapter = adapter

                } else {
                    // Show a message if no merchants are found
                    Toast.makeText(this, "No merchants found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                // Show an error message if the fetch fails
                Log.e("Error", "Failed to fetch merchants", e)
                Toast.makeText(this, "Failed to fetch merchants: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }



}
