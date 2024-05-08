package com.fyp.spotnbook.views.profile.merchant

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.fyp.spotnbook.R
import com.fyp.spotnbook.databinding.ActivityEditMerchantProfileBinding
import com.fyp.spotnbook.model.Gender
import com.fyp.spotnbook.model.Merchant
import com.fyp.spotnbook.model.State
import com.fyp.spotnbook.model.User
import com.fyp.spotnbook.repository.UpdateProfileResult
import com.fyp.spotnbook.viewmodel.AuthenticationViewModel
import com.fyp.spotnbook.viewmodel.ProfileViewModel
import com.fyp.spotnbook.views.profile.ChangeProfilePictureActivity
import com.google.android.material.imageview.ShapeableImageView

class EditMerchantProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditMerchantProfileBinding
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var authenticationViewModel: AuthenticationViewModel

    private lateinit var buttonBack: ImageView
    private lateinit var merchantName: EditText
    private lateinit var profilePicture: ShapeableImageView
    private lateinit var buttonChangeProfileImage: TextView
    private lateinit var address: EditText
    private lateinit var website: EditText
    private lateinit var contactNo: EditText
    private lateinit var businessType: Spinner
    private lateinit var buttonSaveProfile: Button

    private var firstInteract = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_merchant_profile)
        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        authenticationViewModel = ViewModelProvider(this)[AuthenticationViewModel::class.java]
        profileViewModel.getMerchantData(authenticationViewModel.currentUser().toString())

        binding.apply {
            buttonBack = btnReturntoProfile
            merchantName = inputMerchantName
            this@EditMerchantProfileActivity.profilePicture = profilePicture
            buttonChangeProfileImage = changeProfileImage
            address = inputAddress
            website = inputWebsite
            contactNo = inputContact
            businessType = businessTypeSpinner
            buttonSaveProfile = btnSaveProfile
        }

        //----------Observers Result----------
        profileViewModel.updateProfileResult.observe(this) { result ->
            handleUpdateProfileResult(result)
        }

        //----------Adapter for Spinner----------
        //Spinner Item
        val businessTypeSpinnerItem = resources.getStringArray(R.array.businessType)

        //Setup Adapter
        val businessTypeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, businessTypeSpinnerItem)
        businessTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        businessType.adapter = businessTypeAdapter

        //----------Input Filters----------
        address.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(25 * 5))
        merchantName.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(24))

        //----------Get User Data----------
        profileViewModel.merchantData.observe(this) { merchant ->
            Glide.with(this)
                .load(merchant.profileImageUrl)
                .centerCrop()
                .placeholder(R.drawable.anonymous)
                .error(R.drawable.anonymous)
                .into(profilePicture)

            merchantName.setText(merchant.merchantName)
            contactNo.setText(merchant.contactNumber)
            website.setText(merchant.website)
            address.setText(merchant.address)

            //Business Type Spinner
            for (i in 0 until businessTypeSpinnerItem.size) {
                if (businessTypeSpinnerItem[i] == merchant.businessType) {
                    if (i < businessTypeSpinnerItem.size) {
                        businessType.setSelection(i)
                    }
                    break
                }
            }
        }

        //----------UI Events----------
        buttonBack.setOnClickListener{ handleBackButton() }
        buttonSaveProfile.setOnClickListener{ handleSaveButton() }
        buttonChangeProfileImage.setOnClickListener{ handleChangeProfileImage() }
        address.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                val lineCount = address.lineCount

                if (lineCount > 5) {
                    val text = s.toString()
                    val lastLineBreakIndex = text.lastIndexOf('\n')

                    if (lastLineBreakIndex > 0) {
                        val newText = text.substring(0, lastLineBreakIndex)
                        address.setText(newText)
                        address.setSelection(newText.length)
                    }
                }
            }
        })
    }

    private fun handleBackButton(){
        if (isProfileInfoChange() && firstInteract) {
            firstInteract = false
            Toast.makeText(this, "Click back button again to discard changes.", Toast.LENGTH_SHORT).show()
        } else {
            finish()
        }
    }

    private fun handleChangeProfileImage(){
        val intent = Intent(this, ChangeProfilePictureActivity::class.java)
        startActivity(intent)
    }

    private fun handleSaveButton(){
        if (!isProfileInfoChange()){
            finish()
        }
        else{
            val updatedMerchantData = Merchant(
                merchantName = merchantName.text.toString(),
                address = address.text.toString(),
                contactNumber = contactNo.text.toString(),
                website = website.text.toString(),
                businessType = businessType.selectedItem.toString()
            )
            profileViewModel.updateProfile(updatedMerchantData)
        }
    }

    private fun isProfileInfoChange(): Boolean {
        val merchant :Merchant = profileViewModel.merchantData.value!!

        val merchantNameChanged = merchant.merchantName != merchantName.text.toString()
        val addressChanged = merchant.address != address.text.toString()
        val contactNoChanged = merchant.contactNumber != contactNo.text.toString()
        val websiteChanged = merchant.website != website.text.toString()
        val businessTypeChanged = merchant.businessType != businessType.selectedItem.toString()

        return merchantNameChanged || addressChanged || contactNoChanged ||websiteChanged || businessTypeChanged
    }

    private fun handleUpdateProfileResult(result: UpdateProfileResult){
        if (result.isSuccess) {
            Toast.makeText(this, "Profile Updated Successfully.", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            val updateProfileFailedText: String? = result.errorMessage
            Toast.makeText(this, updateProfileFailedText, Toast.LENGTH_SHORT).show()
        }
    }
}