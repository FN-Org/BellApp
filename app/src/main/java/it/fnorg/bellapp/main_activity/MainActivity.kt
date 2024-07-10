package it.fnorg.bellapp.main_activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import it.fnorg.bellapp.login_activity.LogInActivity
import it.fnorg.bellapp.R
import it.fnorg.bellapp.checkConnection
import it.fnorg.bellapp.databinding.MainActivityMainBinding

/**
 * MainActivity is the main entry point of the application. It handles user authentication,
 * navigation, and updating the UI based on user data.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: MainActivityMainBinding

    val viewModel : MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Redirect to login if user is not authenticated
        if (FirebaseAuth.getInstance().currentUser == null) {
            val intent = Intent(this, LogInActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Fetch user data from ViewModel
        viewModel.fetchUserData()

        binding = MainActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up toolbar
        setSupportActionBar(binding.mainToolbar.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        // Configure the AppBarConfiguration with top level destinations
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_add_sys, R.id.nav_settings
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // OnClickListener for signing out
        navView.menu.findItem(R.id.nav_sign_out).setOnMenuItemClickListener {
            AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener {
                    val intent = Intent(this, LogInActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            true
        }

        viewModel.email.observe(this) { email ->
            binding.navView.getHeaderView(0).findViewById<TextView>(R.id.emailTextView).text = email
        }

        viewModel.name.observe(this) { name ->
            binding.navView.getHeaderView(0).findViewById<TextView>(R.id.fullNameTextView).text = name
        }

        viewModel.userImage.observe(this) { userImage ->
            Glide.with(this)
                .load(userImage)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.navView.getHeaderView(0).findViewById(R.id.imageView))
        }

        // Add DrawerListener to call checkConnection() when drawer is opened
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                // Not needed
            }

            override fun onDrawerOpened(drawerView: View) {
                checkConnection(this@MainActivity, binding.root)
                viewModel.fetchUserData()
            }

            override fun onDrawerClosed(drawerView: View) {
                // Not needed
            }

            override fun onDrawerStateChanged(newState: Int) {
                // Not needed
            }
        })
    }

    /**
     * Handles the action bar's Up button navigation.
     *
     * @return True if the navigation was successful, false otherwise.
     */
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}