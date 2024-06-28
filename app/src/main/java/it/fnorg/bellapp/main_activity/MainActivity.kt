package it.fnorg.bellapp.main_activity

import android.content.Intent
import android.os.Bundle
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
import com.firebase.ui.auth.AuthUI
import it.fnorg.bellapp.login_activity.LogInActivity
import it.fnorg.bellapp.R
import it.fnorg.bellapp.databinding.MainActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: MainActivityMainBinding

    val viewModel : MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        viewModel.fetchUserData()

        viewModel.email.observe(this) { email ->
            binding.navView.getHeaderView(0).findViewById<TextView>(R.id.emailTextView).text = email
        }

        viewModel.name.observe(this) { name ->
            binding.navView.getHeaderView(0).findViewById<TextView>(R.id.fullNameTextView).text = name
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}