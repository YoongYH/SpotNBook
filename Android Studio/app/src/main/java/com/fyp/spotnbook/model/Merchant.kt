package com.fyp.spotnbook.model

import com.google.firebase.Timestamp
import java.time.DayOfWeek

data class Merchant(
    var merchantID: String = "",
    var merchantName: String = "",
    var businessType: String = "",
    var address: String = "",
    var profileImageUrl: String = "",
    var website: String = "",
    var authorizedMerchant: Boolean = false,
    var contactNumber: String = "",
    var reviews: ArrayList<MerchantReviews> = ArrayList(),
    var operatingHours: OperatingHours = OperatingHours(),
    var specialClosingDates: List<String> = listOf(),        //List Contain closing date
    var permanentlyClosed: Boolean = false,
    var firstLogin:Boolean = true,
    var followedBy: List<String> = listOf(),
    var following: List<String> = listOf()
)

data class MerchantReviews(
    var userUid: String = "",
    var stars: Int = 0,
    var review: String = "",
    var reviewOn: Timestamp = Timestamp(0, 0),
)

data class OperatingHours(
    val openingHours: Map<String, String> = emptyMap(),
    val closingHours: Map<String, String> = emptyMap()
)