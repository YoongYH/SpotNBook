    package com.fyp.spotnbook.views

    import android.content.Intent
    import android.os.Bundle
    import android.util.Log
    import androidx.appcompat.app.AppCompatActivity
    import androidx.databinding.DataBindingUtil
    import androidx.lifecycle.ViewModelProvider
    import com.fyp.spotnbook.R
    import com.fyp.spotnbook.databinding.ActivityHomePageBinding
    import com.fyp.spotnbook.repository.UserType
    import com.fyp.spotnbook.viewmodel.AuthenticationViewModel
    import com.fyp.spotnbook.viewmodel.ProfileViewModel
    import com.fyp.spotnbook.views.home.HomeFragment
    import com.fyp.spotnbook.views.profile.merchant.MerchantFirstLoginActivity
    import com.fyp.spotnbook.views.profile.merchant.MerchantProfileFragment
    import com.fyp.spotnbook.views.profile.user.EditProfileActivity
    import com.fyp.spotnbook.views.profile.user.UserProfileFragment
    import com.google.android.material.bottomappbar.BottomAppBar
    import com.google.android.material.bottomnavigation.BottomNavigationView
    import com.google.android.material.floatingactionbutton.FloatingActionButton

    class HomePage : AppCompatActivity() {
        private lateinit var binding: ActivityHomePageBinding
        private lateinit var profileViewModel: ProfileViewModel
        private lateinit var authViewModel: AuthenticationViewModel

        private lateinit var bottomAppBar: BottomAppBar
        private lateinit var navBar: BottomNavigationView
        private lateinit var fab: FloatingActionButton

        private lateinit var userType: UserType
        private var lastSelectedItemId: Int = -1

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = DataBindingUtil.setContentView(this, R.layout.activity_home_page)
            profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
            authViewModel = ViewModelProvider(this)[AuthenticationViewModel::class.java]
            profileViewModel.checkUserType(authViewModel.currentUser().toString())


            binding.apply {
                lifecycleOwner = this@HomePage
                this@HomePage.bottomAppBar = bottomAppBar
                this@HomePage.navBar = navBar
                this@HomePage.fab = fab
            }

            //----------Observers----------
            profileViewModel.userType.observe(this) { result ->
                userType = result

                if(userType == UserType.USER){
                    profileViewModel.getUserData(authViewModel.currentUser().toString())

                    profileViewModel.userData.observe(this){ user ->
                        val firstLogin = user.firstLogin
                        if(firstLogin){
                            firstTimeLogin()
                        }
                    }
                }
                else if(userType == UserType.MERCHANT){
                    profileViewModel.getMerchantData(authViewModel.currentUser().toString())

                    profileViewModel.merchantData.observe(this){ merchant ->
                        val firstLogin = merchant.firstLogin
                        if(firstLogin) {
                            firstTimeLogin()
                        }
                    }
                }
            }

            //----------UI Events----------
            navBar.setOnNavigationItemSelectedListener { menuItem ->
                if (lastSelectedItemId == menuItem.itemId) {
                    refreshFragment(menuItem.itemId)
                } else {
                    replaceFragment(menuItem.itemId)
                }

                lastSelectedItemId = menuItem.itemId
                true
            }
            fab.setOnClickListener { fabOnClickAction() }

            //----------Page Initial----------
            setSupportActionBar(bottomAppBar)
            navBar.background = null  // Remove Shadow
            navBar.menu.getItem(1).isEnabled = false

            // Initial Page
            replaceFragment(R.id.nav_Home)
        }

        //----------View Methods----------
        private fun replaceFragment(itemId: Int) {
            val transaction = supportFragmentManager.beginTransaction()

            for (existingFragment in supportFragmentManager.fragments) {
                transaction.hide(existingFragment)
            }

            val fragment = when (itemId) {
                R.id.nav_Home -> HomeFragment()
                R.id.nav_Profile -> {
                    when (userType) {
                        UserType.USER -> UserProfileFragment()
                        UserType.MERCHANT -> MerchantProfileFragment()
                        else -> null
                    }
                }
                else -> null
            }

            fragment?.let {
                if (it.isAdded) {
                    transaction.show(it)
                } else {
                    transaction.add(R.id.fragmentFrame, it)
                }
            }

            transaction.commit()
        }

        private fun refreshFragment(itemId: Int) {
            val newFragment = when (itemId) {
                R.id.nav_Home -> HomeFragment()
                R.id.nav_Profile -> {
                    when (userType) {
                        UserType.USER -> UserProfileFragment()
                        UserType.MERCHANT -> MerchantProfileFragment()
                        else -> null
                    }
                }
                else -> null
            }

            newFragment?.let {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentFrame, it)
                    .commit()
            }
        }

        //Add Post Button
        private fun fabOnClickAction(){
            val intent = Intent(this, AddPostActivity_L::class.java)
            startActivity(intent)
        }

        //If the user is firstTimeLogin
        private fun firstTimeLogin() {
            if(userType == UserType.USER){
                val intent = Intent(this, EditProfileActivity::class.java)
                startActivity(intent)

            }
            else if(userType == UserType.MERCHANT){
                val intent = Intent(this, MerchantFirstLoginActivity::class.java)
                startActivity(intent)
            }
        }
    }