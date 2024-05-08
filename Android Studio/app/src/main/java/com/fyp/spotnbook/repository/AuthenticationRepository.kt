package com.fyp.spotnbook.repository

import com.fyp.spotnbook.model.Merchant
import com.fyp.spotnbook.model.User
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore

//Return Results Data Class
data class LoginUserResult(val isSuccess: Boolean, val errorMessage: String? = null)
data class RegisterResult(val isSuccess: Boolean, val errorMessage: String? = null)
data class ResetPasswordResult(val isSuccess: Boolean, val errorMessage: String? = null)
data class UpdatePasswordResult(val isSuccess: Boolean, val errorMessage: String? = null)

class AuthenticationRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    //Function Login User with Email and Password
    fun loginUser(emailAddress: String, password: String, onComplete: (LoginUserResult) -> Unit) {
        auth.signInWithEmailAndPassword(emailAddress, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onComplete(LoginUserResult(true))
            } else {
                val errorCode = (task.exception as? FirebaseAuthException)?.errorCode
                onComplete(LoginUserResult(false, errorCode ?: "UNKNOWN_ERROR"))
            }
        }
    }

    //Register User With Email and Password
    fun registerUser(emailAddress: String, password: String, username: String, onComplete: (RegisterResult) -> Unit) {
        auth.createUserWithEmailAndPassword(emailAddress, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val userId = currentUser.uid

                    val newUser = User(
                        userID = userId,
                        username = username
                    )

                    firestore.collection("users").document(userId)
                        .set(newUser)
                        .addOnSuccessListener {
                            auth.signOut()
                            onComplete(RegisterResult(true))
                        }
                        .addOnFailureListener { e ->
                            onComplete(RegisterResult(false, e.message ?: "Unknown error"))
                        }
                } else {
                    onComplete(RegisterResult(false, "User not authenticated"))
                }
            } else {
                val errorCode = (task.exception as? FirebaseAuthException)?.errorCode
                onComplete(RegisterResult(false, errorCode ?: "Unknown error"))
            }
        }
    }

    fun registerMerchant(emailAddress: String,password: String,username: String,onComplete: (RegisterResult) -> Unit){
        auth.createUserWithEmailAndPassword(emailAddress, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val userId = currentUser.uid

                    val newMerchant = Merchant(
                        merchantID = userId,
                        merchantName = username
                    )

                    firestore.collection("merchants").document(userId)
                        .set(newMerchant)
                        .addOnSuccessListener {
                            auth.signOut()
                            onComplete(RegisterResult(true))
                        }
                        .addOnFailureListener { e ->
                            onComplete(RegisterResult(false, e.message ?: "Unknown error"))
                        }
                } else {
                    onComplete(RegisterResult(false, "User not authenticated"))
                }
            } else {
                val errorCode = (task.exception as? FirebaseAuthException)?.errorCode
                onComplete(RegisterResult(false, errorCode ?: "Unknown error"))
            }
        }
    }

    //Reset Password
    fun resetPassword(emailAddress: String, onComplete: (ResetPasswordResult) -> Unit) {
        auth.sendPasswordResetEmail(emailAddress).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onComplete(ResetPasswordResult(true))
            } else {
                val errorCode = (task.exception as? FirebaseAuthException)?.errorCode
                onComplete(ResetPasswordResult(false, errorCode ?: "UNKNOWN_ERROR"))
            }
        }
    }

    //Change Password
    fun changePassword(oldPassword: String, newPassword: String, onComplete: (UpdatePasswordResult) -> Unit) {
        val user = auth.currentUser

        if (user != null) {
            val credential = EmailAuthProvider.getCredential(user.email!!, oldPassword)

            user.reauthenticate(credential)
                .addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful) {
                        user.updatePassword(newPassword)
                            .addOnCompleteListener { updatePasswordTask ->
                                if (updatePasswordTask.isSuccessful) {
                                    onComplete(UpdatePasswordResult(true))
                                } else {
                                    val errorCode = (updatePasswordTask.exception as? FirebaseAuthException)?.errorCode
                                    onComplete(UpdatePasswordResult(false, errorCode ?: "UNKNOWN_ERROR"))
                                }
                            }
                    } else {
                        val errorCode = (reauthTask.exception as? FirebaseAuthException)?.errorCode
                        onComplete(UpdatePasswordResult(false, errorCode ?: "UNKNOWN_ERROR"))
                    }
                }
        } else {
            onComplete(UpdatePasswordResult(false, "USER_NOT_LOGGED_IN"))
        }
    }

    //Return Current User UID
    fun currentUser(): String {
        return auth.currentUser!!.uid
    }

    //Logout User
    fun logoutUser(){
        auth.signOut()
    }
}