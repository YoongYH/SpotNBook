package com.fyp.spotnbook.repository

import android.net.Uri
import com.fyp.spotnbook.model.Gender
import com.fyp.spotnbook.model.Merchant
import com.fyp.spotnbook.model.MerchantReviews
import com.fyp.spotnbook.model.OperatingHours
import com.fyp.spotnbook.model.State
import com.fyp.spotnbook.model.User
import com.fyp.spotnbook.repository.UserType.MERCHANT
import com.fyp.spotnbook.repository.UserType.NEITHER
import com.fyp.spotnbook.repository.UserType.USER
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageReference
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class UserType {
    USER, MERCHANT, NEITHER
}

data class GetUserProfileResult(val isSuccess: Boolean, val userData: User? = null, val errorMessage: String? = null)
data class GetMerchantProfileResult(val isSuccess: Boolean, val merchantData: Merchant? = null, val errorMessage: String? = null)
data class UpdateProfileResult(val isSuccess: Boolean, val errorMessage: String? = null)
data class UpdateProfileImageResult(val isSuccess: Boolean, val errorMessage: String? = null)

//Merchant Result
data class UpdateBusinessHoursResult(val isSuccess: Boolean, val errorMessage: String? = null)
data class UpdateClosingDateResult(val isSuccess: Boolean, val errorMessage: String? = null)

class ProfileRepository {
    private val currentUserUid: String = AuthenticationRepository().currentUser()

    //Firebase Storage
    private val storageReference: StorageReference = FirebaseStorage.getInstance().reference

    //Firestore
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")
    private val merchantCollection = firestore.collection("merchants")

    private var userDocument: DocumentReference? = null
    private var merchantDocument: DocumentReference? = null

    fun isUserOrMerchant(uid: String, onComplete: (UserType) -> Unit){
        usersCollection.document(uid).get()
            .addOnSuccessListener { userDoc ->
                if (userDoc.exists()) {
                    onComplete(USER)
                } else {
                    merchantCollection.document(uid).get()
                        .addOnSuccessListener { merchantDoc ->
                            if (merchantDoc.exists()) {
                                onComplete(MERCHANT)
                            } else {
                                onComplete(NEITHER)
                            }
                        }
                        .addOnFailureListener { exception ->
                            onComplete(NEITHER)
                        }
                }
            }
            .addOnFailureListener { exception ->
                onComplete(NEITHER)
            }
    }

    fun firstLoginDone(onComplete: (String?) -> Unit) {
        val uid = currentUserUid
        isUserOrMerchant(uid) { result ->
            val updates = hashMapOf<String, Any>(
                "firstLogin" to false
            )

            val docRef = when (result) {
                USER -> usersCollection.document(uid)
                MERCHANT -> merchantCollection.document(uid)
                NEITHER -> TODO()
            }

            docRef.update(updates)
                .addOnSuccessListener {
                    onComplete("Success")
                }
                .addOnFailureListener { e ->
                    onComplete(e.message)
                }
        }
    }

    //----------Get Data Method----------
    //Get User Data Method
    fun getUserData(uid: String, onDataChanged: (GetUserProfileResult) -> Unit) {
        userDocument = usersCollection.document(uid)

        userDocument?.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                onDataChanged(GetUserProfileResult(false, errorMessage = exception.message))
                return@addSnapshotListener
            }

            //Map Snapshot to User Data
            val userData = snapshot?.toObject(User::class.java)?.apply {
                userID = uid
                setGenderAndState(snapshot)
            }

            if (userData != null) {
                if (userData.imageUrl.isBlank()) {
                    anonymousProfileImageUrl{ url ->
                        userData.imageUrl = url
                    }
                }
                onDataChanged(GetUserProfileResult(true, userData))
            } else {
                onDataChanged(GetUserProfileResult(false, errorMessage = "User data not found"))
            }
        }
    }

    //Get Merchant Data Method
    fun getMerchantData(uid: String, onDataChanged: (GetMerchantProfileResult) -> Unit) {
        merchantDocument = merchantCollection.document(uid)

        merchantDocument?.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                onDataChanged(GetMerchantProfileResult(false, errorMessage = exception.message))
                return@addSnapshotListener
            }

            //Map Snapshot to Merchant Data
            val merchantData = snapshot?.toObject(Merchant::class.java)?.apply {
                merchantID = uid
            }

            if (merchantData != null) {
                if (merchantData.profileImageUrl.isEmpty()) {
                    anonymousProfileImageUrl { url ->
                        merchantData.profileImageUrl = url
                        onDataChanged(GetMerchantProfileResult(true, merchantData))
                    }
                } else {
                    onDataChanged(GetMerchantProfileResult(true, merchantData))
                }
            } else {
                onDataChanged(GetMerchantProfileResult(false))
            }
        }
    }

    //Map value into State and Gender (Explicit for User)
    private fun setGenderAndState(snapshot: DocumentSnapshot) {
        val genderMap = snapshot["gender"] as? Map<*, *>
        val stateMap = snapshot["state"] as? Map<*, *>
        snapshot.toObject(User::class.java)?.apply {
            gender = Gender(
                value = genderMap?.get("value") as? String ?: "",
                display = genderMap?.get("display") as? Boolean ?: true
            )
            state = State(
                value = stateMap?.get("value") as? String ?: "",
                display = stateMap?.get("display") as? Boolean ?: true
            )
        }
    }
    private fun anonymousProfileImageUrl(onUrlReady: (String) -> Unit) {
        val anonymousImageRef = storageReference.child("user_profile/anonymous.png")
        anonymousImageRef.downloadUrl
            .addOnSuccessListener { uri ->
                val imageUrl = uri.toString()
                onUrlReady(imageUrl)
            }
            .addOnFailureListener {
                onUrlReady("")
            }
    }

    //----------Update Data Method----------
    //Update User Profile Method
    fun updateUserData(updatedUserData: User, onComplete: (UpdateProfileResult) -> Unit) {
        val userUID = currentUserUid
        val userRef = usersCollection.document(userUID)

        // Convert User data class to a map
        val userDataMap = mapOf(
            "username" to updatedUserData.username,
            "bio" to updatedUserData.bio,
            "gender" to mapOf(
                "value" to updatedUserData.gender.value,
                "display" to updatedUserData.gender.display
            ),
            "state" to mapOf(
                "value" to updatedUserData.state.value,
                "display" to updatedUserData.state.display
            )
        )

        userRef.set(userDataMap, SetOptions.merge())
            .addOnSuccessListener {
                onComplete(UpdateProfileResult(true))
            }
            .addOnFailureListener { e ->
                onComplete(UpdateProfileResult(false, e.message ?: "UNKNOWN_ERROR"))
            }
    }

    fun updateMerchantData(updatedMerchantData: Merchant, onComplete: (UpdateProfileResult) -> Unit) {
        val userUID = currentUserUid
        val merchantRef = merchantCollection.document(userUID)

        // Convert User data class to a map
        val merchantDataMap = mapOf(
            "merchantName" to updatedMerchantData.merchantName,
            "address" to updatedMerchantData.address,
            "website" to updatedMerchantData.website,
            "contactNumber" to updatedMerchantData.contactNumber,
            "businessType" to updatedMerchantData.businessType
        )

        merchantRef.set(merchantDataMap, SetOptions.merge())
            .addOnSuccessListener {
                onComplete(UpdateProfileResult(true))
            }
            .addOnFailureListener { e ->
                onComplete(UpdateProfileResult(false, e.message ?: "UNKNOWN_ERROR"))
            }
    }

    fun followProfile(targetUserUid: String, onComplete: (UpdateProfileResult) -> Unit) {
        if (targetUserUid.isEmpty() || targetUserUid == currentUserUid) {
            onComplete(UpdateProfileResult(false, "INVALID_TARGET_USER_UID"))
            return
        }

        getCollectionForUser(targetUserUid) { targetCollection ->
            getCollectionForUser(currentUserUid) { currentUserCollection ->
                if (targetCollection == null || currentUserCollection == null) {
                    onComplete(UpdateProfileResult(false, "INVALID_USER_TYPE"))
                    return@getCollectionForUser
                }

                targetCollection.document(targetUserUid).updateFollowedBy(currentUserUid) { targetUpdateResult ->
                    if (targetUpdateResult.isSuccess) {
                        currentUserCollection.document(currentUserUid).updateFollowing(targetUserUid) { currentUserUpdateResult ->
                            onComplete(currentUserUpdateResult)
                        }
                    } else {
                        onComplete(targetUpdateResult)
                    }
                }
            }
        }
    }

    private fun getCollectionForUser(uid: String, onComplete: (CollectionReference?) -> Unit) {
        isUserOrMerchant(uid) { userType ->
            val collectionReference = when (userType) {
                USER -> usersCollection
                MERCHANT -> merchantCollection
                else -> null
            }
            onComplete(collectionReference)
        }
    }

    private fun DocumentReference.updateFollowedBy(uid: String, onComplete: (UpdateProfileResult) -> Unit) {
        get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val followedBy = documentSnapshot["followedBy"] as? ArrayList<String> ?: ArrayList()
                val alreadyFollowing = followedBy.contains(uid)

                followedBy.apply {
                    if (alreadyFollowing) remove(uid) else add(uid)
                }

                update("followedBy", followedBy)
                    .addOnSuccessListener {
                        onComplete(UpdateProfileResult(true))
                    }
                    .addOnFailureListener { e ->
                        onComplete(UpdateProfileResult(false, e.message ?: "UNKNOWN_ERROR"))
                    }
            } else {
                onComplete(UpdateProfileResult(false, "DOCUMENT_NOT_FOUND"))
            }
        }.addOnFailureListener { e ->
            onComplete(UpdateProfileResult(false, e.message ?: "UNKNOWN_ERROR"))
        }
    }
    private fun DocumentReference.updateFollowing(uid: String, onComplete: (UpdateProfileResult) -> Unit) {
        get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val following = documentSnapshot["following"] as? ArrayList<String> ?: ArrayList()

                if (following.contains(uid)) {
                    following.remove(uid)
                } else {
                    following.add(uid)
                }

                update("following", following)
                    .addOnSuccessListener {
                        onComplete(UpdateProfileResult(true))
                    }
                    .addOnFailureListener { e ->
                        onComplete(UpdateProfileResult(false, e.message ?: "UNKNOWN_ERROR"))
                    }
            } else {
                onComplete(UpdateProfileResult(false, "CURRENT_USER_DOCUMENT_NOT_FOUND"))
            }
        }.addOnFailureListener { e ->
            onComplete(UpdateProfileResult(false, e.message ?: "UNKNOWN_ERROR"))
        }
    }

    fun getFollowingList(onComplete: (List<String>) -> Unit) {
        isUserOrMerchant(currentUserUid) { currentUserType ->
            var currentUserProfileReference: DocumentReference?

            when(currentUserType) {
                USER -> {
                    currentUserProfileReference = usersCollection.document(currentUserUid)
                }
                MERCHANT -> {
                    currentUserProfileReference = merchantCollection.document(currentUserUid)
                }
                else -> {
                    onComplete(emptyList())
                    return@isUserOrMerchant
                }
            }

            currentUserProfileReference?.get()?.addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val following = documentSnapshot["following"] as? ArrayList<String> ?: ArrayList()
                    onComplete(following)
                } else {
                    onComplete(emptyList())
                }
            }
                ?.addOnFailureListener {
                    onComplete(emptyList())
                }
        }
    }

    fun addReview(uid: String, review: MerchantReviews, onComplete: (UpdateProfileResult) -> Unit){
        review.userUid = currentUserUid

        val merchantRef = merchantCollection.document(uid)

        merchantRef.update("reviews", FieldValue.arrayUnion(review))
            .addOnSuccessListener {
                onComplete(UpdateProfileResult(true))
            }
            .addOnFailureListener { e ->
                val errorCode = if (e is StorageException) e.errorCode.toString() else "UNKNOWN_ERROR"
                onComplete(UpdateProfileResult(false, errorCode))
            }
    }

    //----------Update Profile Picture----------
    fun updateProfilePicture(data: Any, onComplete: (UpdateProfileImageResult) -> Unit) {
        when (data) {
            is Uri -> uploadToStorage(data, onComplete)
            is String -> writeToFireStore(data, onComplete)
        }
    }
    private fun uploadToStorage(uri: Uri, onComplete: (UpdateProfileImageResult) -> Unit) {
        val uid = currentUserUid
        val imageRef = storageReference.child("user_profile/$uid")
        imageRef.putFile(uri).addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                writeToFireStore(downloadUri.toString(), onComplete)
            }
        }.addOnFailureListener { e ->
            val errorCode = if (e is StorageException) e.errorCode.toString() else "UNKNOWN_ERROR"
            onComplete(UpdateProfileImageResult(false, errorCode))
        }
    }
    private fun writeToFireStore(url: String, onComplete: (UpdateProfileImageResult) -> Unit) {
        val uid = currentUserUid
        isUserOrMerchant(uid){ result ->
            when(result){
                USER -> {
                    val documentRef = usersCollection.document(uid)
                    val updates = mapOf("imageUrl" to url)
                    documentRef.update(updates).addOnSuccessListener { onComplete(UpdateProfileImageResult(true)) }
                        .addOnFailureListener { e -> onComplete(UpdateProfileImageResult(false, e.message ?: "UNKNOWN_ERROR")) }
                }
                MERCHANT -> {
                    val documentRef = merchantCollection.document(uid)
                    val updates = mapOf("profileImageUrl" to url)
                    documentRef.update(updates).addOnSuccessListener { onComplete(UpdateProfileImageResult(true)) }
                        .addOnFailureListener { e -> onComplete(UpdateProfileImageResult(false, e.message ?: "UNKNOWN_ERROR")) }
                }
                else -> {}
            }
        }

    }

    //----------Small Changes of Field----------
    fun merchantUpdateBusinessHour(businessHours: OperatingHours, onComplete: (UpdateBusinessHoursResult) -> Unit){
        val merchantUID = currentUserUid
        val merchantReference = merchantCollection.document(merchantUID)

        merchantReference
            .update("operatingHours", businessHours)
            .addOnSuccessListener {
                onComplete(UpdateBusinessHoursResult(true))
            }
            .addOnFailureListener { e ->
                val errorCode = if (e is StorageException) e.errorCode.toString() else "UNKNOWN_ERROR"
                onComplete(UpdateBusinessHoursResult(false, errorCode))
            }
    }

    fun merchantPermanentClosed(onComplete: (UpdateProfileResult) -> Unit){
        val merchantUID = currentUserUid
        val merchantReference = merchantCollection.document(merchantUID)

        val data = hashMapOf(
            "permanentlyClosed" to true
        )

        merchantReference.set(data).addOnCompleteListener {
            onComplete(UpdateProfileResult(true))
        }.addOnFailureListener{ e ->
            val errorCode = if (e is StorageException) e.errorCode.toString() else "UNKNOWN_ERROR"
            onComplete(UpdateProfileResult(false, errorCode))
        }
    }
    fun merchantReOpen(onComplete: (UpdateProfileResult) -> Unit){
        val merchantUID = currentUserUid
        val merchantReference = merchantCollection.document(merchantUID)

        val data = hashMapOf(
            "permanentlyClosed" to false
        )

        merchantReference.set(data).addOnCanceledListener {
            onComplete(UpdateProfileResult(true))
        }.addOnFailureListener{ e ->
            val errorCode = if (e is StorageException) e.errorCode.toString() else "UNKNOWN_ERROR"
            onComplete(UpdateProfileResult(false, errorCode))
        }
    }

    fun merchantVerify(onComplete: (UpdateProfileResult) -> Unit){
        val merchantUID = currentUserUid
        val merchantReference = merchantCollection.document(merchantUID)

        val data = hashMapOf<String, Any>("authorizedMerchant" to true)

        merchantReference.update(data).addOnSuccessListener {
            onComplete(UpdateProfileResult(true))
        }.addOnFailureListener{ e ->
            val errorCode = if (e is StorageException) e.errorCode.toString() else "UNKNOWN_ERROR"
            onComplete(UpdateProfileResult(false, errorCode))
        }
    }

    fun merchantUpdateClosingDate(date: String, onComplete: (UpdateClosingDateResult) -> Unit){
        val merchantUID = currentUserUid
        val merchantReference = merchantCollection.document(merchantUID)

        merchantReference.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val specialClosingDates = documentSnapshot["specialClosingDates"] as? ArrayList<String>
                    if (specialClosingDates != null) {
                        val updatedDates = if (specialClosingDates.contains(date)) {
                            specialClosingDates.apply { remove(date) }
                        } else {
                            specialClosingDates.apply { add(date) }
                        }

                        // Convert date strings to LocalDate objects for sorting
                        val sortedDates = updatedDates.map { LocalDate.parse(it, DateTimeFormatter.ofPattern("dd MMMM yyyy")) }
                            .sorted()
                            .map { it.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")) }

                        merchantReference.update("specialClosingDates", sortedDates)
                            .addOnSuccessListener {
                                onComplete(UpdateClosingDateResult(true))
                            }
                            .addOnFailureListener { e ->
                                val errorCode = if (e is StorageException) e.errorCode.toString() else "UNKNOWN_ERROR"
                                onComplete(UpdateClosingDateResult(false, errorCode))
                            }
                    } else {
                        onComplete(UpdateClosingDateResult(false, "SPECIAL_CLOSING_DATES_NULL"))
                    }
                } else {
                    onComplete(UpdateClosingDateResult(false, "DOCUMENT_NOT_FOUND"))
                }
            }
            .addOnFailureListener { e ->
                val errorCode = if (e is StorageException) e.errorCode.toString() else "UNKNOWN_ERROR"
                onComplete(UpdateClosingDateResult(false, errorCode))
            }
    }

    fun uploadSSMCertificate(uri: Uri, onComplete: (String) -> Unit) {
        val uid = currentUserUid
        val imageRef = storageReference.child("merchant_ssm_certificates/$uid")
        imageRef.putFile(uri).addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                onComplete(downloadUri.toString())
            }
        }
    }


    //------------------------------------------------------------------------------------------------------
    //Newly added

    //Function to get Merchants collection From Firestore

    fun getMerchantsList(onComplete: (List<Merchant>) -> Unit) {

        val merchantsList = mutableListOf<Merchant>()



        // Get the Firestore collection reference for merchants

        val merchantsRef = FirebaseFirestore.getInstance().collection("merchants")



        // Fetch all documents from the merchants collection

        merchantsRef.get().addOnSuccessListener { documents ->

            for (document in documents) {

                // Convert each document to a Merchant object and add it to the list

                val merchant = document.toObject(Merchant::class.java)

                merchantsList.add(merchant)

            }

            onComplete(merchantsList)

        }.addOnFailureListener { exception ->

            onComplete(emptyList()) // Return an empty list if there's an error

        }

    }
}