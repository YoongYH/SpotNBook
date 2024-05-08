package com.fyp.spotnbook.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fyp.spotnbook.repository.AuthenticationRepository
import com.fyp.spotnbook.repository.LoginUserResult
import com.fyp.spotnbook.repository.RegisterResult
import com.fyp.spotnbook.repository.ResetPasswordResult
import com.fyp.spotnbook.repository.UpdatePasswordResult
import com.google.firebase.auth.FirebaseAuth

class AuthenticationViewModel : ViewModel() {
    private val authenticationRepository = AuthenticationRepository()

    //--------Live Data--------
    private val _loginResult = MutableLiveData<LoginUserResult>()
    val loginResult: LiveData<LoginUserResult> get() = _loginResult

    private val _registerResult = MutableLiveData<RegisterResult>()
    val registerResult: LiveData<RegisterResult> get() = _registerResult

    private val _passwordResetResult = MutableLiveData<ResetPasswordResult>()
    val passwordResetResult: LiveData<ResetPasswordResult> get() = _passwordResetResult

    private val _changePasswordResult = MutableLiveData<UpdatePasswordResult>()
    val changePasswordResult: LiveData<UpdatePasswordResult> get() = _changePasswordResult

    //--------ViewModel Methods--------
    //Login User
    fun loginUser(emailAddress: String, password: String) {
        authenticationRepository.loginUser(emailAddress, password) { result ->
            _loginResult.value = result
        }
    }

    // Register User
    fun registerUser(emailAddress: String, password: String, username: String, registerMerchant: Boolean) {
        val registrationMethod = if (registerMerchant) authenticationRepository::registerMerchant else authenticationRepository::registerUser
        registrationMethod(emailAddress, password, username) { result ->
            _registerResult.value = result
        }
    }

    //Forget Password (Reset Password)
    fun forgetPassword(emailAddress:String){
        authenticationRepository.resetPassword(emailAddress) { result ->
            _passwordResetResult.value = result
        }
    }

    fun changePassword(oldPassword:String, newPassword: String){
        authenticationRepository.changePassword(oldPassword,newPassword) { result ->
            _changePasswordResult.value = result
        }
    }

    //Return current user UID
    fun currentUser(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    //Logout User
    fun logoutUser(){
        authenticationRepository.logoutUser()
    }

    //--------Data Validations--------
    //Function to Check Password Eligibility
    fun passwordEligible(password:String): IntArray {
        val lengthRequirement = password.length >= 8
        val uppercaseRequirement = password.any { it.isUpperCase() }
        val lowercaseRequirement = password.any { it.isLowerCase() }
        val symbolRequirement = password.any { it.isLetterOrDigit().not() }

        return intArrayOf(
            if (lengthRequirement) 1 else 0,
            if (uppercaseRequirement) 1 else 0,
            if (lowercaseRequirement) 1 else 0,
            if (symbolRequirement) 1 else 0
        )
    }
}