package it.fnorg.bellapp.main_activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
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
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import it.fnorg.bellapp.login_activity.LogInActivity
import it.fnorg.bellapp.R
import it.fnorg.bellapp.checkConnection
import it.fnorg.bellapp.databinding.MainActivityMainBinding
import it.fnorg.bellapp.isInternetAvailable

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: MainActivityMainBinding

    val viewModel : MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (FirebaseAuth.getInstance().currentUser == null) {
            val intent = Intent(this, LogInActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding = MainActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.mainToolbar.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_add_sys, R.id.nav_settings
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // OnClickListener for signing-out
        navView.menu.findItem(R.id.nav_sign_out).setOnMenuItemClickListener {
            AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener {
                    val intent = Intent(this, LogInActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            // Per chiudere il drawer dopo il sign-out ma probabilmente non serve
            // drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        viewModel.email.observe(this) { email ->
            binding.navView.getHeaderView(0).findViewById<TextView>(R.id.emailTextView).text = email
        }

        viewModel.name.observe(this) { name ->
            binding.navView.getHeaderView(0).findViewById<TextView>(R.id.fullNameTextView).text = name
        }

        // Add DrawerListener to call checkConnection() when drawer is opened
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                // Not needed
            }

            override fun onDrawerOpened(drawerView: View) {
                checkConnection(this@MainActivity, binding.root)
            }

            override fun onDrawerClosed(drawerView: View) {
                // Not needed
            }

            override fun onDrawerStateChanged(newState: Int) {
                // Not needed
            }
        })

        viewModel.fetchUserData()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}