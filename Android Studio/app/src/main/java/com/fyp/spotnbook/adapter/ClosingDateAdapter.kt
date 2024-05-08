package com.fyp.spotnbook.adapter

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.fyp.spotnbook.R
import com.fyp.spotnbook.repository.AuthenticationRepository
import com.fyp.spotnbook.repository.ProfileRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class ClosingDateAdapter(
    private var dateList: List<String>,
    private var displayArchived: Boolean
) : RecyclerView.Adapter<ClosingDateAdapter.ViewHolder>() {

    private var profileRepository = ProfileRepository()
    private var authenticationRepository = AuthenticationRepository()
    private val filteredDateList: List<String> = filteredList(displayArchived)

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val indexNum: TextView = itemView.findViewById(R.id.indexClosingDate)
        val closingDate: TextView = itemView.findViewById(R.id.dayOfClose)
        val closingDayOfWeek: TextView = itemView.findViewById(R.id.closingDayOfWeek)
        val dayOfWeekBusinessHour: TextView = itemView.findViewById(R.id.dayOfWeekBusinessHour)
        val btnCancel: ImageView = itemView.findViewById(R.id.btnCancel)
        val daysLeft: TextView = itemView.findViewById(R.id.daysLeft)
        val divider: View = itemView.findViewById(R.id.divider)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_closing_date, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context
        val date: String = if (displayArchived) dateList[position] else filteredDateList[position]
        val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")
        val parsedDate: LocalDate = LocalDate.parse(date, formatter)
        val dayOfWeek = formatFirstCharToUpper(parsedDate.dayOfWeek.toString())
        val today: LocalDate = LocalDate.now()

        holder.itemView.visibility = View.VISIBLE

        holder.indexNum.text = (position + 1).toString() + "."

        holder.closingDate.text = date
        holder.closingDayOfWeek.text = "($dayOfWeek)"

        profileRepository.getMerchantData(authenticationRepository.currentUser()) { result ->
            val merchantData = result.merchantData!!
            val openingHours = merchantData.operatingHours.openingHours[parsedDate.dayOfWeek.toString()]
            val closingHours = merchantData.operatingHours.closingHours[parsedDate.dayOfWeek.toString()]
            if (openingHours.isNullOrBlank() && closingHours.isNullOrBlank()) {
                holder.dayOfWeekBusinessHour.text = "Closed on $dayOfWeek"
            } else {
                holder.dayOfWeekBusinessHour.text = "Business Hours: $openingHours ~ $closingHours"
            }
        }

        val dayDifference: Long = ChronoUnit.DAYS.between(today, parsedDate)
        val displayText = when {
            dayDifference == 1L -> "Tomorrow"
            dayDifference == 0L -> "Today"
            dayDifference > 0 -> "$dayDifference days left"
            else -> "Expired"
        }

        holder.daysLeft.text = displayText
        holder.daysLeft.setTextColor(
            ContextCompat.getColor(
                context,
                if (dayDifference > 0) R.color.black else R.color.warning
            )
        )
        holder.daysLeft.setTypeface(null, if (dayDifference in 0..1) Typeface.BOLD else Typeface.NORMAL)

        if (position + 1 == dateList.size && dateList.size != 1) {
            holder.divider.visibility = View.GONE
        }

        holder.btnCancel.setOnClickListener {
            profileRepository.merchantUpdateClosingDate(date) { result ->
                val message = if (result.isSuccess) "Closing Date removed Successfully." else result.errorMessage
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int {
        return if (displayArchived) dateList.size else filteredDateList.size
    }

    private fun formatFirstCharToUpper(input: String): String {
        return input.take(1).uppercase() + input.drop(1).lowercase()
    }

    private fun filteredList(isArchived: Boolean): List<String> {
        val today: LocalDate = LocalDate.now()
        val newList: MutableList<String> = mutableListOf()

        if (!isArchived) {
            for (date in dateList) {
                val parsedDate: LocalDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd MMMM yyyy"))
                if (!parsedDate.isBefore(today)) {
                    newList.add(date)
                }
            }
        }
        return newList
    }

    fun updateData(newDateList: List<String>) {
        dateList = newDateList
        notifyDataSetChanged()
    }

    fun updateDisplayArchived(displayArchived: Boolean) {
        this.displayArchived = displayArchived
        notifyDataSetChanged()
    }
}