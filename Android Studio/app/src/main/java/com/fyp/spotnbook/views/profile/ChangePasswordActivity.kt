package com.fyp.spotnbook.views.profile

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.fyp.spotnbook.R
import com.fyp.spotnbook.databinding.ActivityChangePasswordBinding
import com.fyp.spotnbook.repository.UpdatePasswordResult
import com.fyp.spotnbook.viewmodel.AuthenticationViewModel
import com.fyp.spotnbook.views.authentication.LoginActivity

class ChangePasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangePasswordBinding
    private lateinit var authenticationViewModel: AuthenticationViewModel

    private lateinit var btnBack: ImageView
    private lateinit var oldPassword: EditText
    private lateinit var newPassword: EditText
    private lateinit var confirmPassword: EditText
    private lateinit var btnChangePassword: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_change_password)
        binding.apply {
            this@ChangePasswordActivity.btnBack = btnBack
            oldPassword = inputOldPassword
            newPassword = inputNewPassword
            confirmPassword = inputConfirmPassword
            this@ChangePasswordActivity.btnChangePassword = btnChangePassword
            lifecycleOwner = this@ChangePasswordActivity
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
        oldPassword.filters = arrayOf(noSpaceFilter)
        newPassword.filters = arrayOf(noSpaceFilter)
        confirmPassword.filters = arrayOf(noSpaceFilter)

        //--------Observers--------
        authenticationViewModel.changePasswordResult.observe(this, Observer { result ->
            handleChangePasswordResult(result)
        })

        //--------UI Events--------
        btnBack.setOnClickListener { finish() }
        btnChangePassword.setOnClickListener{ handleChangePassword(oldPassword.text.toString(), newPassword.text.toString(), confirmPassword.text.toString()) }

        //Dynamic Password Hint
        newPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(editable: Editable?) {
                updatePasswordEligibilityUI()
            }
        })
    }

    private fun handleChangePasswordResult(result: UpdatePasswordResult){
        if (result.isSuccess) {
            Toast.makeText(this, "Password updated successfully. Redirecting to the login screen.", Toast.LENGTH_SHORT).show()

            authenticationViewModel.logoutUser()

            //Return to LoginActivity and Clear Activity in Stacks
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            this.finishAffinity()
        } else {
            val changePasswordFailedText: String? = result.errorMessage
            Toast.makeText(this, changePasswordFailedText, Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleChangePassword(oldPassword: String, newPassword: String, confirmPassword: String) {
        when {
            isFieldEmpty(oldPassword, newPassword) -> showToast("Please fill in all fields.")
            confirmPassword != newPassword -> showToast("Confirm password does not match with New Password, Please Try Again.")
            newPassword == oldPassword -> showToast("New Password is the same as Old Password, Try a new one.")
            authenticationViewModel.passwordEligible(newPassword).all { it == 1 } -> {
                authenticationViewModel.changePassword(oldPassword, newPassword)
            }
        }
    }

    private fun isFieldEmpty(vararg fields: String): Boolean {
        return fields.any { it.isBlank() }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun updatePasswordEligibilityUI() {
        val password: String = binding.inputNewPassword.text.toString()

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
