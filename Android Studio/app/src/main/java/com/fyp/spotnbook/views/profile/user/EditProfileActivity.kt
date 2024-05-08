package com.fyp.spotnbook.views.profile.user

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.fyp.spotnbook.R
import com.fyp.spotnbook.databinding.ActivityEditProfileBinding
import com.fyp.spotnbook.model.Gender
import com.fyp.spotnbook.model.State
import com.fyp.spotnbook.model.User
import com.fyp.spotnbook.repository.UpdateProfileResult
import com.fyp.spotnbook.viewmodel.AuthenticationViewModel
import com.fyp.spotnbook.viewmodel.ProfileViewModel
import com.fyp.spotnbook.views.profile.ChangeProfilePictureActivity
import com.google.android.material.imageview.ShapeableImageView

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var authenticationViewModel: AuthenticationViewModel

    private lateinit var buttonBack: ImageView
    private lateinit var username: EditText
    private lateinit var profilePicture: ShapeableImageView
    private lateinit var buttonChangeProfileImage: TextView
    private lateinit var bio: EditText
    private lateinit var gender: Spinner
    private lateinit var state: Spinner
    private lateinit var genderDisplay: SwitchCompat
    private lateinit var stateDisplay: SwitchCompat
    private lateinit var buttonSaveProfile: Button

    private var firstInteract = true
    private var userFirstLogin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_profile)
        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        authenticationViewModel = ViewModelProvider(this)[AuthenticationViewModel::class.java]
        profileViewModel.getUserData(authenticationViewModel.currentUser().toString())

        binding.apply {
            username = inputUsername
            bio = inputBio
            gender = genderSpinner
            state = stateSpinner
            this@EditProfileActivity.profilePicture = profilePicture
            buttonChangeProfileImage = changeProfileImage
            buttonSaveProfile = btnSaveProfile
            buttonBack = btnReturntoProfile
            genderDisplay = switchDisplayGender
            stateDisplay = switchDisplayState
        }

        //----------Observers Result----------
        profileViewModel.updateProfileResult.observe(this, Observer { result ->
            handleUpdateProfileResult(result)
        })

        //----------Adapter for Spinner----------
        //Spinner Item
        val genderSpinnerItem = resources.getStringArray(R.array.gender)
        val stateSpinnerItem = resources.getStringArray(R.array.state)

        //Setup Adapter
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genderSpinnerItem)
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        gender.adapter = genderAdapter

        val stateAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, stateSpinnerItem)
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        state.adapter = stateAdapter

        //----------Input Filters----------
        bio.filters = arrayOf<InputFilter>(LengthFilter(25 * 5))
        username.filters = arrayOf<InputFilter>(LengthFilter(12))

        //----------Get User Data----------
        profileViewModel.userData.observe(this) { user ->
            if (user.firstLogin) {
                userFirstLogin = true
                buttonBack.visibility = View.GONE
            }

            Glide.with(this)
                .load(user.imageUrl)
                .centerCrop()
                .placeholder(R.drawable.anonymous)
                .error(R.drawable.anonymous)
                .into(profilePicture)

            username.setText(user.username)
            bio.setText(user.bio)

            for (i in 0 until genderSpinnerItem.size) {
                if (genderSpinnerItem[i] == user.gender.value) {
                    if (i < genderSpinnerItem.size) {
                        gender.setSelection(i)
                    }
                    break
                }
            }

            for (i in 0 until stateSpinnerItem.size) {
                if (stateSpinnerItem[i] == user.state.value) {
                    if (i < stateSpinnerItem.size) {
                        state.setSelection(i)
                    }
                    break
                }
            }

            genderDisplay.isChecked = user.gender.display
            stateDisplay.isChecked = user.state.display
        }

        //----------UI Events----------
        buttonBack.setOnClickListener{ handleBackButton() }
        buttonSaveProfile.setOnClickListener{ handleSaveButton() }
        buttonChangeProfileImage.setOnClickListener{ handleChangeProfileImage() }
        bio.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                val lineCount = bio.lineCount

                if (lineCount > 5) {
                    val text = s.toString()
                    val lastLineBreakIndex = text.lastIndexOf('\n')

                    if (lastLineBreakIndex > 0) {
                        val newText = text.substring(0, lastLineBreakIndex)
                        bio.setText(newText)
                        bio.setSelection(newText.length)
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
        if (!isProfileInfoChange() && !userFirstLogin){
            finish()
        }
        else{
            val updatedUserData = User(
                username = username.text.toString(),
                bio = bio.text.toString(),
                gender = Gender(value = gender.selectedItem.toString(), display = genderDisplay.isChecked),
                state = State(value = state.selectedItem.toString(), display = stateDisplay.isChecked)
            )
            profileViewModel.updateProfile(updatedUserData)

            if(userFirstLogin){
                profileViewModel.firstLoginComplete()
            }
        }
    }

    private fun isProfileInfoChange(): Boolean {
        val user = profileViewModel.userData.value

        val usernameChanged = user!!.username != username.text.toString()
        val bioChanged = user.bio != bio.text.toString()
        val genderChanged = user.gender.value != gender.selectedItem.toString()
        val stateChanged = user.state.value != state.selectedItem.toString()
        val genderDisplayChanged = user.gender.display != genderDisplay.isChecked
        val stateDisplayChanged = user.state.display != stateDisplay.isChecked

        return usernameChanged || bioChanged || genderChanged || stateChanged || genderDisplayChanged || stateDisplayChanged
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
