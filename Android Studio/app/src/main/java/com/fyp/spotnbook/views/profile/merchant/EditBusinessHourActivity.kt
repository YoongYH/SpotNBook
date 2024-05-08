package com.fyp.spotnbook.views.profile.merchant

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.fyp.spotnbook.R
import com.fyp.spotnbook.databinding.ActivityEditBusinessHourBinding
import com.fyp.spotnbook.model.OperatingHours
import com.fyp.spotnbook.repository.UpdateBusinessHoursResult
import com.fyp.spotnbook.viewmodel.AuthenticationViewModel
import com.fyp.spotnbook.viewmodel.ProfileViewModel
import java.util.Locale

class EditBusinessHourActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditBusinessHourBinding
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var authenticationViewModel: AuthenticationViewModel

    private val dayContainers: List<LinearLayout> by lazy {
        listOf(
            findViewById(R.id.mondayContainer),
            findViewById(R.id.tuesdayContainer),
            findViewById(R.id.wednesdayContainer),
            findViewById(R.id.thursdayContainer),
            findViewById(R.id.fridayContainer),
            findViewById(R.id.saturdayContainer),
            findViewById(R.id.sundayContainer)
        )
    }

    private val openingHourTextViews: List<TextView> by lazy {
        listOf(
            findViewById(R.id.mondayOpeningHour),
            findViewById(R.id.tuesdayOpeningHour),
            findViewById(R.id.wednesdayOpeningHour),
            findViewById(R.id.thursdayOpeningHour),
            findViewById(R.id.fridayOpeningHour),
            findViewById(R.id.saturdayOpeningHour),
            findViewById(R.id.sundayOpeningHour)
        )
    }

    private val closingHourTextViews: List<TextView> by lazy {
        listOf(
            findViewById(R.id.mondayClosingHour),
            findViewById(R.id.tuesdayClosingHour),
            findViewById(R.id.wednesdayClosingHour),
            findViewById(R.id.thursdayClosingHour),
            findViewById(R.id.fridayClosingHour),
            findViewById(R.id.saturdayClosingHour),
            findViewById(R.id.sundayClosingHour)
        )
    }

    private val closedButtons: List<ImageView> by lazy {
        listOf(
            findViewById(R.id.btnClosedMonday),
            findViewById(R.id.btnClosedTuesday),
            findViewById(R.id.btnClosedWednesday),
            findViewById(R.id.btnClosedThursday),
            findViewById(R.id.btnClosedFriday),
            findViewById(R.id.btnClosedSaturday),
            findViewById(R.id.btnClosedSunday)
        )
    }

    private lateinit var btnBack: ImageView
    private lateinit var btnAddOpeningDay: ImageView
    private lateinit var addOpeningDayLayout: LinearLayout
    private lateinit var linkPermanentlyClosedContainer: LinearLayout
    private lateinit var linkPermanentlyClosed: TextView
    private lateinit var btnSaveBusinessHour: Button

    private var isDataChanged = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_business_hour)
        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        authenticationViewModel = ViewModelProvider(this)[AuthenticationViewModel::class.java]
        profileViewModel.getMerchantData(authenticationViewModel.currentUser().toString())

        binding.apply {
            this@EditBusinessHourActivity.btnBack = btnBack
            this@EditBusinessHourActivity.btnAddOpeningDay = btnAddOpeningDay
            this@EditBusinessHourActivity.addOpeningDayLayout = addOpeningDayLayout
            this@EditBusinessHourActivity.linkPermanentlyClosedContainer = linkPermanentlyClosedContainer
            this@EditBusinessHourActivity.linkPermanentlyClosed = linkPermanentlyClosed
            this@EditBusinessHourActivity.btnSaveBusinessHour = btnSaveBusinessHour
        }

        //----------Observer----------
        profileViewModel.merchantData.observe(this) { merchant ->
            dayContainers.forEachIndexed { index, container ->
                val day = getDayOfWeekFromIndex(index)
                val openingHour = merchant.operatingHours.openingHours[day]
                val closingHour = merchant.operatingHours.closingHours[day]

                if (!openingHour.isNullOrBlank() && !closingHour.isNullOrBlank()) {
                    openingHourTextViews[index].text = openingHour
                    closingHourTextViews[index].text = closingHour
                } else {
                    container.visibility = View.GONE
                }
            }

            val allClosed = isAllClosed()
            val allDisplay = isAllDisplay()
            addOpeningDayLayout.visibility = if (allDisplay) View.GONE else View.VISIBLE

            if(merchant.firstLogin){
                linkPermanentlyClosedContainer.visibility = View.GONE
            }
            else{
                linkPermanentlyClosedContainer.visibility = if (allClosed) View.VISIBLE else View.GONE
            }
        }

        profileViewModel.updateMerchantOperatingHoursResult.observe(this) { result ->
            handleUpdateMerchantOperatingHoursResult(result)
        }

        //---------UI Events----------
        btnBack.setOnClickListener {
            if (isDataChanged) {
                showConfirmationDialog()
            } else {
                finish()
            }
        }

        linkPermanentlyClosed.setOnClickListener {
            val intent = Intent(this@EditBusinessHourActivity, PermanentlyClosedActivity::class.java)
            startActivity(intent)
            finish()
        }

        setClosedButtonClickListeners()

        // Set click listeners for opening and closing hours for each day
        setDayOfWeekClickListeners(openingHourTextViews)
        setDayOfWeekClickListeners(closingHourTextViews)

        btnAddOpeningDay.setOnClickListener { view ->
            val popUpMenu = PopupMenu(this@EditBusinessHourActivity, view)
            popUpMenu.inflate(R.menu.day_of_week)

            val menu = popUpMenu.menu

            dayContainers.forEachIndexed { index, container ->
                if (container.visibility == View.VISIBLE) {
                    menu.removeItem(getDayMenuItemId(index))
                }
            }

            popUpMenu.setOnMenuItemClickListener { menuItem ->
                val dayIndex = getDayIndexFromMenuItemId(menuItem.itemId)
                if (dayIndex != -1) {
                    dayContainers[dayIndex].visibility = View.VISIBLE
                    addOpeningDayLayout.visibility = if (isAllDisplay()) View.GONE else View.VISIBLE
                    linkPermanentlyClosedContainer.visibility = if (isAllClosed()) View.VISIBLE else View.GONE
                    true
                } else {
                    false
                }
            }
            popUpMenu.show()
        }

        btnSaveBusinessHour.setOnClickListener { handleSaveButton() }
    }

    private fun setClosedButtonClickListeners() {
        closedButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                dayContainers[index].visibility = View.GONE
                addOpeningDayLayout.visibility = if (isAllDisplay()) View.GONE else View.VISIBLE
                linkPermanentlyClosedContainer.visibility = if (isAllClosed()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun setDayOfWeekClickListeners(textViews: List<TextView>) {
        textViews.forEach { textView ->
            textView.setOnClickListener {
                showTimePicker(textView)
            }
        }
    }

    private fun showTimePicker(textView: TextView) {
        val currentTime = textView.text.toString()
        val (startHour, startMinute) = convertTimeStringToStartTime(currentTime)

        val timePickerDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                val formattedHour = if (hourOfDay < 12) {
                    if (hourOfDay == 0) 12 else hourOfDay
                } else {
                    if (hourOfDay == 12) hourOfDay else hourOfDay - 12
                }
                val amPm = if (hourOfDay < 12) "am" else "pm"
                val formattedTime = String.format(Locale.getDefault(), "%d:%02d %s", formattedHour, minute, amPm)

                if (formattedTime != currentTime) {
                    isDataChanged = true
                }

                textView.text = formattedTime
            },
            startHour,
            startMinute,
            false
        )

        timePickerDialog.show()
    }

    private fun convertTimeStringToStartTime(timeString: String): Pair<Int, Int> {
        val timeComponents = timeString.split(" ")

        if (timeComponents.size == 2) {
            val hourMinuteComponents = timeComponents[0].split(":")
            val hour = hourMinuteComponents[0].toInt()
            val minute = hourMinuteComponents[1].toInt()

            val adjustedHour = if (timeComponents[1].equals("pm", ignoreCase = true) && hour != 12) {
                hour + 12
            } else if (timeComponents[1].equals("am", ignoreCase = true) && hour == 12) {
                0
            } else {
                hour
            }

            return Pair(adjustedHour, minute)
        }
        return Pair(0, 0)
    }

    // Check Container Visibility
    private fun isAllDisplay(): Boolean {
        return dayContainers.all { it.visibility == View.VISIBLE }
    }

    private fun isAllClosed(): Boolean {
        return dayContainers.all { it.visibility == View.GONE }
    }

    // Save Business Hour
    private fun handleSaveButton() {
        if (isAllClosed()) {
            Toast.makeText(this, "No Business Hours inserted, At Least 1 Day is required.", Toast.LENGTH_SHORT).show()
        } else {
            if(isDataChanged){
                val updatedOperatingHours = populateOperatingHours()
                profileViewModel.updateBusinessHours(updatedOperatingHours)
            }
            else{
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun populateOperatingHours(): OperatingHours {
        val updatedOpeningHours = mutableMapOf<String, String>()
        val updatedClosingHours = mutableMapOf<String, String>()

        dayContainers.forEachIndexed { index, container ->
            if (container.visibility == View.VISIBLE) {
                val day = getDayOfWeekFromIndex(index)
                updatedOpeningHours[day] = openingHourTextViews[index].text.toString()
                updatedClosingHours[day] = closingHourTextViews[index].text.toString()
            }
        }

        return OperatingHours(openingHours = updatedOpeningHours, closingHours = updatedClosingHours)
    }

    // Result Handle
    private fun handleUpdateMerchantOperatingHoursResult(result: UpdateBusinessHoursResult) {
        if (result.isSuccess) {
            Toast.makeText(this, "Business Hours Updated Successfully.", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            val updateFailedText: String? = result.errorMessage
            Toast.makeText(this, updateFailedText, Toast.LENGTH_SHORT).show()
        }
    }

    private fun getDayOfWeekFromIndex(index: Int): String {
        return when (index) {
            0 -> "MONDAY"
            1 -> "TUESDAY"
            2 -> "WEDNESDAY"
            3 -> "THURSDAY"
            4 -> "FRIDAY"
            5 -> "SATURDAY"
            6 -> "SUNDAY"
            else -> throw IllegalArgumentException("Invalid day index: $index")
        }
    }

    private fun getDayMenuItemId(index: Int): Int {
        return when (index) {
            0 -> R.id.monday
            1 -> R.id.tuesday
            2 -> R.id.wednesday
            3 -> R.id.thursday
            4 -> R.id.friday
            5 -> R.id.saturday
            6 -> R.id.sunday
            else -> -1
        }
    }

    private fun getDayIndexFromMenuItemId(itemId: Int): Int {
        return when (itemId) {
            R.id.monday -> 0
            R.id.tuesday -> 1
            R.id.wednesday -> 2
            R.id.thursday -> 3
            R.id.friday -> 4
            R.id.saturday -> 5
            R.id.sunday -> 6
            else -> -1
        }
    }

    private fun showConfirmationDialog() {
        AlertDialog.Builder(this)
            .setMessage("You have unsaved changes. Are you sure you want to go back?")
            .setPositiveButton("Discard Changes") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}