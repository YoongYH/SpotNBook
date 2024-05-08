package com.fyp.spotnbook.views.profile.merchant

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fyp.spotnbook.R
import com.fyp.spotnbook.adapter.ClosingDateAdapter
import com.fyp.spotnbook.databinding.ActivityEditClosingDateBinding
import com.fyp.spotnbook.repository.UpdateClosingDateResult
import com.fyp.spotnbook.viewmodel.AuthenticationViewModel
import com.fyp.spotnbook.viewmodel.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditClosingDateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditClosingDateBinding
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var authenticationViewModel: AuthenticationViewModel

    private lateinit var btnBack: ImageView
    private lateinit var btnShowArchived: TextView
    private lateinit var btnAddClosingDate: ImageView
    private lateinit var closingDateRecyclerView: RecyclerView

    private var displayArchived: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_closing_date)
        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        authenticationViewModel = ViewModelProvider(this)[AuthenticationViewModel::class.java]
        profileViewModel.getMerchantData(authenticationViewModel.currentUser().toString())

        binding.apply {
            this@EditClosingDateActivity.btnBack = btnBack
            this@EditClosingDateActivity.closingDateRecyclerView = closingDateRecyclerView
            btnAddClosingDate = btnAddClosingDay
            this@EditClosingDateActivity.btnShowArchived = btnShowArchived
        }

        //----------Observers----------
        profileViewModel.merchantData.observe(this) { merchant ->
            val adapter = ClosingDateAdapter(merchant.specialClosingDates, displayArchived)
            closingDateRecyclerView.adapter = adapter
            closingDateRecyclerView.layoutManager = LinearLayoutManager(this)

            adapter.updateData(merchant.specialClosingDates)

            btnAddClosingDate.setOnClickListener {
                handleAddClosingDate { selectedDate ->
                    if (merchant.specialClosingDates.contains(selectedDate)) {
                        Toast.makeText(this, "Selected Date is already Exist.", Toast.LENGTH_SHORT).show()
                    } else {
                        profileViewModel.updateClosingDate(selectedDate)
                    }
                }
            }
        }

        profileViewModel.updateClosingDateResult.observe(this) { result ->
            handleUpdateClosingDateResult(result)
        }

        //----------UI Events----------
        btnBack.setOnClickListener { finish() }
        btnShowArchived.setOnClickListener {
            if (displayArchived) {
                btnShowArchived.text = getString(R.string.show_archived_date)
            } else {
                btnShowArchived.text = getString(R.string.hide_archived_date)
            }
            displayArchived = !displayArchived
            updateAdapter(displayArchived)
        }
    }

    private fun updateAdapter(displayArchived: Boolean) {
        this.displayArchived = displayArchived
        closingDateRecyclerView.adapter?.let { adapter ->
            if (adapter is ClosingDateAdapter) {
                adapter.updateDisplayArchived(displayArchived)
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun handleAddClosingDate(callback: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val minDateCalendar = Calendar.getInstance()

        val maxDateCalendar = Calendar.getInstance()
        maxDateCalendar.add(Calendar.MONTH, 3)

        val datePickerDialog = DatePickerDialog(
            this,
            { view: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDayOfMonth: Int ->
                val calendar = Calendar.getInstance()
                calendar.set(selectedYear, selectedMonth, selectedDayOfMonth)

                val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                val formattedDate = dateFormat.format(calendar.time)

                // Pass the formatted date to the callback
                callback(formattedDate)
            },
            year,
            month,
            dayOfMonth
        )

        datePickerDialog.datePicker.minDate = minDateCalendar.timeInMillis
        datePickerDialog.datePicker.maxDate = maxDateCalendar.timeInMillis

        datePickerDialog.show()
    }

    private fun handleUpdateClosingDateResult(result: UpdateClosingDateResult) {
        if (result.isSuccess) {
            Toast.makeText(this, "Closing Date added Successfully.", Toast.LENGTH_SHORT).show()
        } else {
            val updateFailedText: String? = result.errorMessage
            Toast.makeText(this, updateFailedText, Toast.LENGTH_SHORT).show()
        }
    }
}