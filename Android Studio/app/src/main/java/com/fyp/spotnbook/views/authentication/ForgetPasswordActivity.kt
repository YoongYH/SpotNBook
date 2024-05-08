package com.fyp.spotnbook.views.authentication

import android.os.Bundle
import android.text.InputFilter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.fyp.spotnbook.R
import com.fyp.spotnbook.databinding.ActivityForgetPasswordBinding
import com.fyp.spotnbook.databinding.ActivityForgetPasswordSuccessBinding
import com.fyp.spotnbook.viewmodel.AuthenticationViewModel

class ForgetPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgetPasswordBinding
    private lateinit var binding_success_view: ActivityForgetPasswordSuccessBinding
    private lateinit var authenticationViewModel: AuthenticationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_forget_password)
        binding.lifecycleOwner = this

        authenticationViewModel = ViewModelProvider(this).get(AuthenticationViewModel::class.java)

        //Input Filter
        val noSpaceFilter = InputFilter { source, start, end, dest, dstart, dend ->
            for (i in start until end) {
                if (Character.isWhitespace(source[i])) {
                    return@InputFilter ""
                }
            }
            null
        }

        binding.inputEmail.filters = arrayOf(noSpaceFilter)

        //--------Observers--------
        authenticationViewModel.passwordResetResult.observe(this, Observer { result ->
            if (result.isSuccess) {
                Toast.makeText(this, "Password Reset Email is successfully sent!", Toast.LENGTH_SHORT).show()
                binding_success_view = DataBindingUtil.setContentView(this, R.layout.activity_forget_password_success)

                binding_success_view.btnBacktoLogin.setOnClickListener{
                    finish()
                }
            } else {
                //Password Reset Failed Actions
                var resetFailedText: String? = result.errorMessage

                when {
                    result.errorMessage == "ERROR_INVALID_EMAIL" -> {
                        resetFailedText = "Invalid Email Format, Please check again."
                    }
                    result.errorMessage == "ERROR_TOO_MANY_REQUESTS" -> {
                        resetFailedText = "Too many attempts! Try again later."
                    }
                    result.errorMessage == "ERROR_MISSING_EMAIL" -> {
                        resetFailedText = "Please enter a valid email address to proceed."
                    }
                }
                Toast.makeText(this, resetFailedText, Toast.LENGTH_SHORT).show()
            }
        })

        //--------Button Click Events--------
        //Reset button Click Event
        binding.btnResetPassword.setOnClickListener {
            val emailAddress: String = binding.inputEmail.text.toString()
            if (emailAddress.isNotEmpty()){
                authenticationViewModel.forgetPassword(emailAddress)
            }
            else{
                Toast.makeText(this, "Please enter an email address to reset password.", Toast.LENGTH_SHORT).show()
            }
        }

        //End Current Activity (Return to Login Screen)
        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}