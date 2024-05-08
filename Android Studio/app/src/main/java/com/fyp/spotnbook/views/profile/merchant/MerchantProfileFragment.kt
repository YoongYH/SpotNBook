package com.fyp.spotnbook.views.profile.merchant

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.fyp.spotnbook.R
import com.fyp.spotnbook.adapter.MerchantProfilePageTabAdapter
import com.fyp.spotnbook.databinding.FragmentMerchantProfileBinding
import com.fyp.spotnbook.viewmodel.AuthenticationViewModel
import com.fyp.spotnbook.viewmodel.PostViewModel
import com.fyp.spotnbook.viewmodel.ProfileViewModel
import com.fyp.spotnbook.views.authentication.LoginActivity
import com.fyp.spotnbook.views.profile.ChangePasswordActivity
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.tabs.TabLayout
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MerchantProfileFragment : Fragment() {
    private lateinit var binding: FragmentMerchantProfileBinding
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var authenticationViewModel: AuthenticationViewModel
    private lateinit var postViewModel: PostViewModel

    private lateinit var profilePicture: ShapeableImageView
    private lateinit var iconVerified: ImageView
    private lateinit var merchantName: TextView
    private lateinit var merchantUid: TextView
    private lateinit var numFollowers: TextView
    private lateinit var numPersonReviewed: TextView
    private lateinit var operationHourLayout: LinearLayout
    private lateinit var textOperatingStatus: TextView
    private lateinit var textOperatingHours: TextView
    private lateinit var textAddress: TextView
    private lateinit var businessType: TextView
    private lateinit var btnPhoneCall: ImageView
    private lateinit var btnFollow: CardView
    private lateinit var textFollow: TextView
    private lateinit var textWebsite: TextView
    private lateinit var merchantWebsiteContainer: LinearLayout

    private lateinit var merchant_tab: TabLayout
    private lateinit var merchant_viewpage: ViewPager2
    private lateinit var profileNavDrawer: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_merchant_profile, container, false
        )

        //-----ViewModels-----
        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        authenticationViewModel = ViewModelProvider(this)[AuthenticationViewModel::class.java]
        postViewModel = ViewModelProvider(this)[PostViewModel::class.java]

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            this@MerchantProfileFragment.profilePicture = profilePicture
            iconVerified = verified
            this@MerchantProfileFragment.merchantName = merchantName
            merchantUid = profileUID
            numFollowers = numberFollowers
            numPersonReviewed = numberPersonReviewed
            operationHourLayout = operatingHours
            textOperatingStatus = txtOpening
            textOperatingHours = txtOpeningHours
            this@MerchantProfileFragment.btnPhoneCall = btnPhoneCall
            textAddress = merchantAddress
            businessType = businessTypeTextView
            btnFollow = followCardView
            textFollow = followTextView
            textWebsite = merchantWebsite
            this@MerchantProfileFragment.merchantWebsiteContainer = merchantWebsiteContainer

            merchant_tab = merchantProfileTabLayout
            merchant_viewpage = merchantProfileViewPager
            this@MerchantProfileFragment.profileNavDrawer = profileNavDrawer
        }

        //Get UID to View Profile
        val uid: String = (arguments?.getString("uid") ?: authenticationViewModel.currentUser()).toString()
        profileViewModel.getMerchantData(uid)

        //Hide Drawer Icon
        if(uid != authenticationViewModel.currentUser().toString()){
            profileNavDrawer.visibility = View.GONE
        }

        //----------Observers----------
        profileViewModel.merchantData.observe(viewLifecycleOwner) { merchant ->
            //Assign Value
            merchantName.text = merchant.merchantName
            merchantUid.text = "UID: " + merchant.merchantID
            textAddress.text = merchant.address
            businessType.text = merchant.businessType
            numFollowers.text = merchant.followedBy.size.toString()
            numPersonReviewed.text = merchant.reviews.size.toString()


            val today: LocalDate = LocalDate.now()
            val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")
            val formattedDate: String = today.format(formatter)

            val closingDate: List<String> = merchant.specialClosingDates
            val isTodayClosingDate: Boolean = closingDate.contains(formattedDate)

            if(isTodayClosingDate){
                textOperatingStatus.text = "Closed Today"
                textOperatingStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.warning))
            }
            else{
                //Operating Hours
                val currentTime = Calendar.getInstance()
                val dayOfWeekInt = currentTime.get(Calendar.DAY_OF_WEEK)

                val dayOfWeekString = when (dayOfWeekInt) {
                    Calendar.SUNDAY -> "SUNDAY"
                    Calendar.MONDAY -> "MONDAY"
                    Calendar.TUESDAY -> "TUESDAY"
                    Calendar.WEDNESDAY -> "WEDNESDAY"
                    Calendar.THURSDAY -> "THURSDAY"
                    Calendar.FRIDAY -> "FRIDAY"
                    Calendar.SATURDAY -> "SATURDAY"
                    else -> throw IllegalArgumentException("Invalid day of week: $dayOfWeekInt")
                }

                if(merchant.permanentlyClosed){
                    textOperatingStatus.text = "Permanently Closed"
                    textOperatingStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.warning))
                }
                else{
                    val openingHours: String? = merchant.operatingHours.openingHours[dayOfWeekString]
                    val closingHours: String? = merchant.operatingHours.closingHours[dayOfWeekString]

                    if (!openingHours.isNullOrBlank() && !closingHours.isNullOrBlank()) {
                        textOperatingHours.visibility = View.VISIBLE
                        if (isWithinOperatingHours(openingHours, closingHours)) {
                            textOperatingStatus.text = "Opening"
                            textOperatingStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.success))
                        } else {
                            textOperatingStatus.text = "Closed"
                            textOperatingStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.warning))
                        }
                        textOperatingHours.text = ", $openingHours ~ $closingHours"
                    } else {
                        textOperatingHours.visibility = View.GONE
                        textOperatingStatus.text = "Closed Today"
                        textOperatingStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.warning))
                    }
                }
            }

            //Profile Image
            Glide.with(this)
                .load(merchant.profileImageUrl)
                .centerCrop()
                .placeholder(R.drawable.anonymous)
                .error(R.drawable.anonymous)
                .into(profilePicture)

            //Display Profile Image Icon Verified
            iconVerified.visibility = if (merchant.authorizedMerchant) View.VISIBLE else View.GONE

            //Copy to Clipboard
            merchantUid.setOnClickListener {
                val clipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboardManager.setPrimaryClip(ClipData.newPlainText(null, merchant.merchantID))
                Toast.makeText(requireContext(), "UID Copied to Clipboard", Toast.LENGTH_SHORT).show()
            }

            textAddress.setOnClickListener {
                val clipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboardManager.setPrimaryClip(ClipData.newPlainText(null, merchant.address))
                Toast.makeText(requireContext(), "Address Copied to Clipboard", Toast.LENGTH_SHORT).show()
            }

            //Website Container
            if (merchant.website.isBlank()){
                merchantWebsiteContainer.visibility = View.GONE
            }
            else{
                textWebsite.setPaintFlags(textWebsite.getPaintFlags() or Paint.UNDERLINE_TEXT_FLAG) //Underline Text
                textWebsite.text = merchant.website
                textWebsite.setOnClickListener { intentWebsite(merchant.website) }
            }

            //Operation Hours
            operationHourLayout.setOnClickListener { navigateToBusinessHour(merchant.merchantID) }

            //Phone Call
            if(merchant.contactNumber.isBlank()){
                btnPhoneCall.visibility = View.GONE
            }
            else{
                btnPhoneCall.setOnClickListener { intentCall(merchant.contactNumber) }
            }

            //Follow Button
            btnFollow.setOnClickListener{
                if(uid == authenticationViewModel.currentUser()){
                    Toast.makeText(requireContext(), "You cant follow yourself.", Toast.LENGTH_SHORT).show()
                }
                else{
                    profileViewModel.followProfile(uid)
                }
            }

            if (merchant.followedBy.contains(authenticationViewModel.currentUser().toString())) {
                textFollow.text = "Following"
                btnFollow.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.input_border))
            } else {
                textFollow.text = "Follow"
                btnFollow.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary))
            }
        }

        //----------Tab Switch----------
        // Initialize Adapter
        merchant_viewpage.adapter = MerchantProfilePageTabAdapter(this, uid)

        // Add Tabs
        merchant_tab.addTab(merchant_tab.newTab().setText("Reviews"))
        merchant_tab.addTab(merchant_tab.newTab().setText("Posts"))
        merchant_tab.addTab(merchant_tab.newTab().setText("Tagged Posts"))

        // Tab Layout Listener
        merchant_tab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let { merchant_viewpage.currentItem = it.position }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

        // Page Change Listener
        merchant_viewpage.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                merchant_tab.getTabAt(position)?.select()
            }
        })

        //----------Navigation Drawer----------
        // Set Logout Item Text Color to Red
        setLogoutItemTextColor()

        // Initialize ActionBarDrawerToggle and sync state
        val drawerlayout = binding.profileDrawerLayout
        val toggle = ActionBarDrawerToggle(requireActivity(), drawerlayout, R.string.profile, R.string.home)
        drawerlayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.menu.removeItem(R.id.nav_editProfile)

        // NavigationView Item Selection Listener
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            val intent = when (menuItem.itemId) {
                R.id.nav_editProfile -> Intent(requireContext(), EditMerchantProfileActivity::class.java)
                R.id.nav_changePassword -> Intent(requireContext(), ChangePasswordActivity::class.java)
                R.id.nav_merchantCentre -> Intent(requireContext(), MerchantCentreActivity::class.java)
                else -> null
            }
            intent?.let {
                startActivity(it)
                true
            } ?: when (menuItem.itemId) {
                R.id.nav_logOut -> {
                    logout()
                    true
                }
                else -> false
            }
        }

        //----------UI Events----------
        // Display Navigation Drawer when drawer icon is clicked
        binding.profileNavDrawer.setOnClickListener {
            binding.profileDrawerLayout.openDrawer(GravityCompat.END)
        }

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        Glide.with(this).clear(profilePicture)
    }

    private fun navigateToBusinessHour(uid: String){
        val intent = Intent(requireContext(), BusinessHourActivity::class.java)
        intent.putExtra("merchantId", uid)
        startActivity(intent)
    }

    private fun intentCall(merchantNumber: String){
        val phoneNumber = merchantNumber

        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        startActivity(intent)
    }

    private fun intentWebsite(website: String){
        val url = "https://" + website
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun setLogoutItemTextColor() {
        val navView = binding.navView
        val logoutItem = navView.menu.findItem(R.id.nav_logOut)

        val spannable = SpannableString(logoutItem.title)
        spannable.setSpan(ForegroundColorSpan(Color.RED), 0, spannable.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        logoutItem.title = spannable
    }

    private fun logout() {
        // Intent to Login Page
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        requireActivity().finish()

        Toast.makeText(requireContext(), "Logout Successful.", Toast.LENGTH_SHORT).show()
        authenticationViewModel.logoutUser()
    }

    fun convertStringToTime(timeString: String): Calendar {
        val format = SimpleDateFormat("h:mm a", Locale.getDefault())
        format.timeZone = TimeZone.getTimeZone("GMT")
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
        calendar.time = format.parse(timeString) ?: Date()
        return calendar
    }

    fun isWithinOperatingHours(openingHours: String, closingHours: String): Boolean {
        val currentTime = Calendar.getInstance()

        val openingTime = convertStringToTime(openingHours)
        val closingTime = convertStringToTime(closingHours)

        currentTime.set(Calendar.SECOND, 0)
        currentTime.set(Calendar.MILLISECOND, 0)

        val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)
        val currentMinute = currentTime.get(Calendar.MINUTE)

        val openingHour = openingTime.get(Calendar.HOUR_OF_DAY)
        val openingMinute = openingTime.get(Calendar.MINUTE)

        val closingHour = closingTime.get(Calendar.HOUR_OF_DAY)
        val closingMinute = closingTime.get(Calendar.MINUTE)

        return (currentHour > openingHour || (currentHour == openingHour && currentMinute >= openingMinute)) &&
                (currentHour < closingHour || (currentHour == closingHour && currentMinute < closingMinute))
    }
}