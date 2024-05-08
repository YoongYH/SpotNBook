package com.fyp.spotnbook.model

data class User(
    var userID: String = "",
    var username: String = "",
    var imageUrl: String = "",
    var gender: Gender = Gender(),
    var state: State = State(),
    var bio: String = "",
    var firstLogin: Boolean = true,
    var followedBy: List<String> = listOf(),
    var following: List<String> = listOf()
)

data class Gender(
    val value: String = "Prefer not to Tell",
    val display: Boolean = true // Display on Profile?
)

data class State(
    val value: String = "Prefer not to Tell",
    val display: Boolean = true // Display on Profile?
)