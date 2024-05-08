    package com.fyp.spotnbook.views.profile

    import android.content.Intent
    import android.graphics.Bitmap
    import android.graphics.ImageDecoder
    import android.graphics.drawable.Drawable
    import android.net.Uri
    import android.os.Build
    import android.os.Bundle
    import android.provider.MediaStore
    import android.text.Editable
    import android.text.InputFilter
    import android.text.TextWatcher
    import android.widget.Button
    import android.widget.EditText
    import android.widget.ImageView
    import android.widget.Toast
    import androidx.activity.result.contract.ActivityResultContracts
    import androidx.appcompat.app.AppCompatActivity
    import androidx.databinding.DataBindingUtil
    import androidx.lifecycle.Observer
    import androidx.lifecycle.ViewModelProvider
    import com.bumptech.glide.Glide
    import com.bumptech.glide.load.engine.GlideException
    import com.bumptech.glide.request.RequestListener
    import com.bumptech.glide.request.target.Target
    import com.fyp.spotnbook.R
    import com.fyp.spotnbook.databinding.ActivityChangeProfilePictureBinding
    import com.fyp.spotnbook.repository.UpdateProfileImageResult
    import com.fyp.spotnbook.viewmodel.ProfileViewModel
    import com.google.android.material.imageview.ShapeableImageView
    import java.io.File
    import java.io.FileOutputStream
    import java.io.IOException
    import java.io.OutputStream

    class ChangeProfilePictureActivity : AppCompatActivity() {
        private lateinit var binding: ActivityChangeProfilePictureBinding
        private lateinit var profileViewModel: ProfileViewModel

        private lateinit var backButton: ImageView
        private lateinit var previewImage: ShapeableImageView
        private lateinit var buttonUploadImage: Button
        private lateinit var buttonSaveImage: Button
        private lateinit var imageUrl: EditText

        private val PICK_IMAGE_REQUEST = 1
        private var latestAction: LatestAction = LatestAction.NONE
        private var selectedImageUri: Uri? = null

        private enum class LatestAction {
            NONE, UPLOAD_IMAGE, INSERT_URL, URL_INVALID
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = DataBindingUtil.setContentView(this, R.layout.activity_change_profile_picture)
            profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

            binding.apply {
                backButton = btnReturntoEditProfile
                this@ChangeProfilePictureActivity.previewImage = previewImage
                buttonUploadImage = btnUploadImage
                buttonSaveImage = btnSaveImage
                imageUrl = inputImageUrl
                lifecycleOwner = this@ChangeProfilePictureActivity
            }

            //----------Input Filter----------
            val noSpaceFilter = InputFilter { source, start, end, dest, dstart, dend ->
                for (i in start until end) {
                    if (Character.isWhitespace(source[i])) {
                        return@InputFilter ""
                    }
                }
                null
            }

            imageUrl.filters = arrayOf(noSpaceFilter)

            //----------Observers----------
            //Get User Data
            profileViewModel.userData.observe(this) { user ->
                Glide.with(this)
                    .load(user.imageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.anonymous)
                    .error(R.drawable.anonymous)
                    .into(previewImage)
            }

            profileViewModel.updateProfileImageResult.observe(this, Observer { result ->
                handleUpdateProfileImageResult(result)
           })

            //----------UI Events----------
            backButton.setOnClickListener{ handleBackButton() }
            buttonUploadImage.setOnClickListener { handleUploadImage() }
            buttonSaveImage.setOnClickListener { handleSaveImage() }
            imageUrl.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    previewImageURL(imageUrl.text.toString())
                }
            })
        }

        private var clickedTwice = false
        private fun handleBackButton() {
            when (latestAction) {
                LatestAction.UPLOAD_IMAGE, LatestAction.INSERT_URL -> {
                    if (clickedTwice != true){
                        Toast.makeText(this, "Click again to discard changes.", Toast.LENGTH_SHORT).show()
                        clickedTwice = true
                    }
                    else{
                        finish()
                    }
                }
                else -> {
                    finish()
                }
            }
        }

        private fun handleUploadImage() {
            getContent.launch("image/*")
        }

        private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                val selectedBitmap: Bitmap = getBitmapFromUri(uri)
                val tempFile: File = saveBitmapToTempFile(selectedBitmap)

                Glide.with(this)
                    .load(tempFile)
                    .centerCrop()
                    .into(previewImage)

                latestAction = LatestAction.UPLOAD_IMAGE
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)

            if (resultCode == RESULT_OK && requestCode == PICK_IMAGE_REQUEST) {
                val imageUri: Uri = data?.data!!

                val selectedBitmap: Bitmap = getBitmapFromUri(imageUri)

                val tempFile: File = saveBitmapToTempFile(selectedBitmap)

                Glide.with(this)
                    .load(tempFile)
                    .centerCrop()
                    .into(previewImage)

                latestAction = LatestAction.UPLOAD_IMAGE
            }
        }

        private fun saveBitmapToTempFile(bitmap: Bitmap): File {
            val tempFile = File.createTempFile("temp_image", ".jpg", cacheDir)

            try {
                val os: OutputStream = FileOutputStream(tempFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
                os.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return tempFile
        }

        @Suppress("DEPRECATION")
        private fun getBitmapFromUri(uri: Uri): Bitmap {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            }
        }

        private fun handleSaveImage(){
            when (latestAction) {
                LatestAction.UPLOAD_IMAGE -> selectedImageUri?.let { updateProfileImage(it) }
                LatestAction.INSERT_URL -> updateProfileImage(imageUrl.text.toString().trim())
                LatestAction.URL_INVALID -> Toast.makeText(applicationContext, "Image URL is Invalid, Try a new one.", Toast.LENGTH_SHORT).show()
                else -> {
                    Toast.makeText(applicationContext, "No image uploaded or URL provided.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun previewImageURL(url: String){
            Glide.with(this)
                .load(url)
                .centerCrop()
                .error(R.drawable.icon_cancel)
                .listener(object : RequestListener<Drawable?> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable?>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Toast.makeText(applicationContext, "Error Preview Image, Try another URL?", Toast.LENGTH_SHORT).show()
                        latestAction = LatestAction.URL_INVALID
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable?>?,
                        dataSource: com.bumptech.glide.load.DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        latestAction = LatestAction.INSERT_URL
                        return false
                    }
                })
                .into(previewImage)
        }

        private fun updateProfileImage(data: Any) {
            when (data) {
                is Uri -> profileViewModel.updateProfileImage(data)
                is String -> profileViewModel.updateProfileImage(data)
            }
        }

        private fun handleUpdateProfileImageResult(result: UpdateProfileImageResult){
            if (result.isSuccess) {
                Toast.makeText(this, "Profile Image Change Successfully.", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                val updateProfileImageFailedText: String? = result.errorMessage
                Toast.makeText(this, updateProfileImageFailedText, Toast.LENGTH_SHORT).show()
            }
        }
    }