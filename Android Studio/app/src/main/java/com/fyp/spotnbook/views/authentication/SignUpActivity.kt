package com.fyp.spotnbook.views.authentication

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.fyp.spotnbook.R
import com.fyp.spotnbook.databinding.ActivitySignUpBinding
import com.fyp.spotnbook.databinding.ActivitySignUpSuccessBinding
import com.fyp.spotnbook.repository.RegisterResult
import com.fyp.spotnbook.viewmodel.AuthenticationViewModel

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var binding_success_view: ActivitySignUpSuccessBinding
    private lateinit var authenticationViewModel: AuthenticationViewModel

    private lateinit var email: EditText
    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var confirmPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)

        authenticationViewModel = ViewModelProvider(this).get(AuthenticationViewModel::class.java)

        email = binding.inputRegisterEmail
        username = binding.inputRegisterUsername
        password = binding.inputRegisterPassword
        confirmPassword = binding.inputRegisterConfirmPassword

        // Input Filters
        val noSpaceFilter = InputFilter { source, start, end, dest, dstart, dend ->
            for (i in start until end) {
                if (Character.isWhitespace(source[i])) {
                    return@InputFilter ""
                }
            }
            null
        }

        val usernameMaxLengthFilter = InputFilter.LengthFilter(40)
        val passwordMaxLengthFilter = InputFilter.LengthFilter(18)

        email.filters = arrayOf(noSpaceFilter)
        username.filters = arrayOf(usernameMaxLengthFilter)
        password.filters = arrayOf(noSpaceFilter, passwordMaxLengthFilter)
        confirmPassword.filters = arrayOf(noSpaceFilter, passwordMaxLengthFilter)

        //--------Observers--------
        authenticationViewModel.registerResult.observe(this, Observer { result ->
            handleRegisterResult(result)
        })

        //--------UI Events--------
        binding.btnBack.setOnClickListener { finish() }
        binding.btnRegister.setOnClickListener {
            val email: String = email.text.toString().trim()
            val username: String = username.text.toString().trim()
            val password: String = password.text.toString().trim()
            val confirmPassword: String = confirmPassword.text.toString().trim()
            val registerForMerchant = binding.registerForMerchant.isChecked

            if(email.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty()){
                if(password == confirmPassword){
                    if(authenticationViewModel.passwordEligible(password).all { it == 1 }){
                        authenticationViewModel.registerUser(email,password,username,registerForMerchant)
                    }
                    else{
                        Toast.makeText(this, "Password wasn't Strong Enough!", Toast.LENGTH_SHORT).show()
                    }
                }
                else{
                    Toast.makeText(this, "Password does not Match, Check and Try Again!", Toast.LENGTH_SHORT).show()
                }
            }
            else{
                Toast.makeText(this, "Please make sure all field are filled.", Toast.LENGTH_SHORT).show()
            }
        }

        //Toggle Password Hint when input Password on Focus
        binding.inputRegisterPassword.setOnFocusChangeListener { v, hasFocus ->
            togglePasswordHintVisibility(hasFocus)
        }

        //Dynamic Password Hint
        binding.inputRegisterPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(editable: Editable?) {
                updatePasswordEligibilityUI()
            }
        })
    }

    private fun handleRegisterResult(result: RegisterResult){
        if (result.isSuccess) {
            Toast.makeText(this, "Account Registered Successfully.", Toast.LENGTH_SHORT).show()
            binding_success_view = DataBindingUtil.setContentView(this, R.layout.activity_sign_up_success)

            binding_success_view.btnBacktoLogin.setOnClickListener{
                finish()
            }
        } else {
            //Register Failed Actions
            var registerFailedText: String? = result.errorMessage

            when {
                result.errorMessage == "ERROR_INVALID_EMAIL" -> {
                    registerFailedText = "Email Address is not valid!"
                }
            }
            Toast.makeText(this, registerFailedText, Toast.LENGTH_SHORT).show()
        }
    }

    private fun togglePasswordHintVisibility(hasFocus: Boolean) {
        val passwordHint = binding.passwordHint
        passwordHint.visibility = if (hasFocus) View.VISIBLE else View.GONE
    }

    private fun updatePasswordEligibilityUI() {
        val password: String = binding.inputRegisterPassword.text.toString()

        val hint_length = binding.passwordHintLength
        val hint_capital = binding.passwordHintCapital
        val hint_lower = binding.passwordHintLower
        val hint_symbol = binding.passwordHintSymbol

        val success_color = resources.getColor(R.color.success)
        val failed_color = resources.getColor(R.color.warning)

        val passwordEligibility = authenticationViewModel.passwordEligible(password)

        updatePasswordHintUI(hint_length, passwordEligibility[0], success_color, failed_color)
        updatePasswordHintUI(hint_capital, passwordEligibility[1], success_color, failed_color)
        updatePasswordHintUI(hint_lower, passwordEligibility[2], success_color, failed_color)
        updatePasswordHintUI(hint_symbol, passwordEligibility[3], success_color, failed_color)
    }

    private fun updatePasswordHintUI(view: TextView, eligibility: Int, successColor: Int, failedColor: Int) {
        when {
            eligibility == 1 -> view.setTextColor(successColor)
            eligibility == 0 -> view.setTextColor(failedColor)
        }
    }
}