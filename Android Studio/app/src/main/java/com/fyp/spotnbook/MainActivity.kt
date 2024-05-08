package com.fyp.spotnbook

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.fyp.spotnbook.views.HomePage
import com.fyp.spotnbook.views.authentication.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            // User is signed in, redirect to HomePage.kt
            val intent = Intent(this, HomePage::class.java)
            startActivity(intent)
        } else {
            // No user is signed in, redirect to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        finish()
    }
}