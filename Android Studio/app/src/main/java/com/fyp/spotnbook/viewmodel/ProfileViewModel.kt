package com.fyp.spotnbook.viewmodel

import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fyp.spotnbook.model.Merchant
import com.fyp.spotnbook.model.MerchantReviews
import com.fyp.spotnbook.model.OperatingHours
import com.fyp.spotnbook.model.User
import com.fyp.spotnbook.repository.ProfileRepository
import com.fyp.spotnbook.repository.UpdateBusinessHoursResult
import com.fyp.spotnbook.repository.UpdateClosingDateResult
import com.fyp.spotnbook.repository.UpdateProfileImageResult
import com.fyp.spotnbook.repository.UpdateProfileResult
import com.fyp.spotnbook.repository.UserType

class ProfileViewModel() : ViewModel() {
    private val profileRepository = ProfileRepository()

    //----------Live Data----------
    private val _firstLoginResult = MutableLiveData<String?>()
    val firstLoginResult: LiveData<String?> get() = _firstLoginResult

    private val _userType = MutableLiveData<UserType>()
    val userType: LiveData<UserType> get() = _userType

    private val _userData = MutableLiveData<User>()
    val userData: LiveData<User> get() = _userData

    private val _merchantData = MutableLiveData<Merchant>()
    val merchantData: LiveData<Merchant> get() = _merchantData

    private val _updateProfileResult = MutableLiveData<UpdateProfileResult>()
    val updateProfileResult: LiveData<UpdateProfileResult> get() = _updateProfileResult

    private val _updateProfileImageResult = MutableLiveData<UpdateProfileImageResult>()
    val updateProfileImageResult: LiveData<UpdateProfileImageResult> get() = _updateProfileImageResult

    private val _updateMerchantOperatingHoursResult = MutableLiveData<UpdateBusinessHoursResult>()
    val updateMerchantOperatingHoursResult: LiveData<UpdateBusinessHoursResult> get() = _updateMerchantOperatingHoursResult

    private val _updateClosingDateResult = MutableLiveData<UpdateClosingDateResult>()
    val updateClosingDateResult: LiveData<UpdateClosingDateResult> get() = _updateClosingDateResult

    //----------Methods----------
    //Get UserData with UID
    fun getUserData(uid: String) {
        profileRepository.getUserData(uid) { result ->
            _userData.postValue(result.userData ?: User())
        }
    }

    //Get Merchant Data with UID
    fun getMerchantData(uid: String) {
        profileRepository.getMerchantData(uid) { result ->
            _merchantData.postValue(result.merchantData ?: Merchant())
        }
    }

    fun firstLoginComplete(){
        profileRepository.firstLoginDone(){ result ->
            _firstLoginResult.postValue(result)
        }
    }

    //-----Update Profile Data-----
    //Method Overloading for Update Profile Data (User and Merchant)
    fun updateProfile(userData: User) {
        if (userData.username.isBlank()) {
            _updateProfileResult.value = UpdateProfileResult(false, "Username cannot be empty")
        } else {
            profileRepository.updateUserData(userData) { result ->
                _updateProfileResult.value = result
            }
        }
    }
    fun updateProfile(merchantData: Merchant) {
        if (merchantData.merchantName.isBlank()) {
            _updateProfileResult.value = UpdateProfileResult(false, "Merchant name cannot be empty")
        }
        else if (merchantData.address.isBlank()) {
            _updateProfileResult.value = UpdateProfileResult(false, "Address cannot be empty")
        }else {
            profileRepository.updateMerchantData(merchantData) { result ->
                _updateProfileResult.value = result
            }
        }
    }

    fun followProfile(targetUserUid: String){
        profileRepository.followProfile(targetUserUid){}
    }

    //-----Update Profile Image-----
    //Method Overloading for update Profile Image (Using Uri or Url)
    fun updateProfileImage(uri: Uri) {
        profileRepository.updateProfilePicture(uri) { result ->
            _updateProfileImageResult.value = result
        }
    }
    fun updateProfileImage(url: String) {
        profileRepository.updateProfilePicture(url) { result ->
            _updateProfileImageResult.value = result
        }
    }

    //-----Check User Type-----
    fun checkUserType(uid: String) {
        profileRepository.isUserOrMerchant(uid) { userType ->
            _userType.postValue(userType)
        }
    }

    //-----Merchant Method-----
    fun updateBusinessHours(updatedBusinessHours: OperatingHours){
        profileRepository.merchantUpdateBusinessHour(updatedBusinessHours){ result ->
            _updateMerchantOperatingHoursResult.postValue(result)
        }
    }

    fun updateClosingDate(closingDate: String){
        profileRepository.merchantUpdateClosingDate(closingDate){ result ->
            _updateClosingDateResult.postValue(result)
        }
    }

    fun verifyMerchant() {
        profileRepository.merchantVerify { result ->
            _updateProfileResult.postValue(result)
        }
    }

    fun uploadSSMCertificate(uri: Uri, onComplete: (String) -> Unit) {
        profileRepository.uploadSSMCertificate(uri) { result ->
            onComplete(result)
        }
    }

    fun addReview(uid:String, review: MerchantReviews){
        profileRepository.addReview(uid, review) { result ->
            _updateProfileResult.postValue(result)
        }
    }
}
