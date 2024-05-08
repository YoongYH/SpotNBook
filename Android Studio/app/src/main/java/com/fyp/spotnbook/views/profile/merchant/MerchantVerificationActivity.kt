package com.fyp.spotnbook.views.profile.merchant

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract.Profile
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.fyp.spotnbook.R
import com.fyp.spotnbook.databinding.ActivityMerchantVerificationBinding
import com.fyp.spotnbook.repository.UpdateProfileResult
import com.fyp.spotnbook.viewmodel.ProfileViewModel
import com.fyp.spotnbook.views.profile.ChangeProfilePictureActivity
import com.google.android.material.imageview.ShapeableImageView
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class MerchantVerificationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMerchantVerificationBinding
    private lateinit var profileViewModel: ProfileViewModel

    private lateinit var btnBack: ImageView
    private lateinit var btnVerify: Button

    private lateinit var btnUpload: Button
    private lateinit var merchantName: EditText
    private lateinit var registrationNo: EditText
    private lateinit var ssmPreview: ShapeableImageView

    private lateinit var scannedResultLayout: LinearLayout
    private lateinit var scannedErrorLayout: LinearLayout

    private var selectedImageUri: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_merchant_verification)
        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        binding.apply {
            this@MerchantVerificationActivity.btnBack = btnBack
            this@MerchantVerificationActivity.btnVerify = btnVerify
            this@MerchantVerificationActivity.btnUpload = btnUpload
            this@MerchantVerificationActivity.merchantName = merchantName
            this@MerchantVerificationActivity.registrationNo = registrationNo

            this@MerchantVerificationActivity.ssmPreview = ssmPreview
        }

        //-----Observers-----
        profileViewModel.updateProfileResult.observe(this){ result ->
            handleResult(result)
        }

        //-----UI Events-----
        btnBack.setOnClickListener { finish() }
        btnUpload.setOnClickListener { handleUploadButton() }
        btnVerify.setOnClickListener { handleVerifyButton() }
    }

    private fun handleVerifyButton(){
        if (merchantName.text.isNullOrBlank() && registrationNo.text.isNullOrBlank()){
            Toast.makeText(this, "Please upload SSM Certificate to proceed.", Toast.LENGTH_SHORT).show()
        }
        else{
            profileViewModel.verifyMerchant()
        }
    }

    private fun handleUploadButton(){
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
                .into(ssmPreview)

            profileViewModel.uploadSSMCertificate(selectedImageUri!!){ result ->
                sendImageToServer(result)
            }
        }
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

    private fun handleResult(result: UpdateProfileResult){
        if (result.isSuccess) {
            Toast.makeText(this, "Merchant Verified Successfully", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            val updateFailedText: String? = result.errorMessage
            Toast.makeText(this, updateFailedText, Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendImageToServer(imageUrl: String) {
        val client = OkHttpClient()

        // Create Request
        val json = JSONObject()
        json.put("image_url", imageUrl)
        val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), json.toString())

        val request = Request.Builder()
            .url("http://192.168.1.120:5000/extract_text")
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

                        val scannedMerchantName = jsonObject.optString("merchant_name")
                        val scannedRegistrationNo = jsonObject.optString("registration_no")

                        runOnUiThread {
                            merchantName.setText(scannedMerchantName)
                            registrationNo.setText(scannedRegistrationNo)
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
        })
    }
}
