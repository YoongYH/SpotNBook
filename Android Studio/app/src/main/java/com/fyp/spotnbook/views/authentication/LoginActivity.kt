package com.fyp.spotnbook.views.authentication

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.fyp.spotnbook.R
import com.fyp.spotnbook.databinding.ActivityLoginBinding
import com.fyp.spotnbook.repository.LoginUserResult
import com.fyp.spotnbook.viewmodel.AuthenticationViewModel
import com.fyp.spotnbook.views.HomePage
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var authenticationViewModel: AuthenticationViewModel

    private lateinit var email: EditText
    private lateinit var password: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.apply {
            email = inputEmail
            password = inputPassword
            lifecycleOwner = this@LoginActivity
        }
        authenticationViewModel = ViewModelProvider(this).get(AuthenticationViewModel::class.java)

        //--------Input Filter--------
        val noSpaceFilter = InputFilter { source, start, end, dest, dstart, dend ->
            for (i in start until end) {
                if (Character.isWhitespace(source[i])) {
                    return@InputFilter ""
                }
            }
            null
        }
        email.filters = arrayOf(noSpaceFilter)
        password.filters = arrayOf(noSpaceFilter)

        //--------Observers--------
        authenticationViewModel.loginResult.observe(this, Observer { result ->
            handleLoginResult(result)
        })

        //--------UI Events--------
        binding.btnLogin.setOnClickListener { handleLoginButtonClick() }
        binding.linkRegister.setOnClickListener { navigateToSignUp() }
        binding.linkForgetPassword.setOnClickListener { navigateToForgetPassword() }
        binding.inputPassword.setOnFocusChangeListener { _, hasFocus -> handlePasswordFocusChange(hasFocus) }
        binding.togglePassword.setOnClickListener { togglePasswordVisibility() }
    }

    private fun handleLoginResult(result: LoginUserResult) {
        if (result.isSuccess) {
            val intent = Intent(this, HomePage::class.java)
            startActivity(intent)
            finish()
        } else {
            //Login Failed Actions
            var loginFailedText: String? = result.errorMessage

            when {
                result.errorMessage == "ERROR_INVALID_EMAIL" -> {
                    loginFailedText = "Invalid Email Address Format."
                }

                result.errorMessage == "ERROR_INVALID_CREDENTIAL" -> {
                    loginFailedText = "Email Address or Password Incorrect!"
                }

                result.errorMessage == "ERROR_USER_DISABLED" -> {
                    loginFailedText =
                        "Your account has been disabled, contact administrator for help!"
                }

                result.errorMessage == "ERROR_TOO_MANY_REQUESTS" -> {
                    loginFailedText = "Too many attempts! Try again later."
                }
            }
            Toast.makeText(this, loginFailedText, Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleLoginButtonClick() {
        val emailAddress: String = binding.inputEmail.text.toString().trim()
        val password: String = binding.inputPassword.text.toString().trim()
        if(emailAddress.isNotEmpty() && password.isNotEmpty()){
            authenticationViewModel.loginUser(emailAddress,password)
        }
        else{
            if(!emailAddress.isNotEmpty()){
                Toast.makeText(this, "Please enter a valid Email Address.", Toast.LENGTH_SHORT).show()
            }
            if(!password.isNotEmpty()){
                Toast.makeText(this, "Please enter password to continue.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handlePasswordFocusChange(hasFocus: Boolean) {
        if (hasFocus) {
            binding.togglePassword.visibility = View.VISIBLE
        } else {
            binding.togglePassword.visibility = View.GONE
            binding.inputPassword.transformationMethod = PasswordTransformationMethod.getInstance()
        }
    }

    private fun togglePasswordVisibility() {
        val passwordVisibility = binding.inputPassword.transformationMethod

        if (passwordVisibility == null) {
            binding.inputPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            binding.togglePassword.setImageResource(R.drawable.icon_password_eye_slash_solid)
        } else {
            binding.inputPassword.transformationMethod = null
            binding.togglePassword.setImageResource(R.drawable.icon_password_eye_solid)
        }
        binding.inputPassword.setSelection(binding.inputPassword.text.length)
    }

    private fun navigateToSignUp() {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToForgetPassword() {
        val intent = Intent(this, ForgetPasswordActivity::class.java)
        startActivity(intent)
    }
}