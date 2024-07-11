package it.fnorg.bellapp.welcome_activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.google.firebase.auth.FirebaseAuth
import it.fnorg.bellapp.R
import it.fnorg.bellapp.main_activity.MainActivity

/**
 * Activity representing the welcome sequence of the application.
 */
class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if the user is already logged in
        if (FirebaseAuth.getInstance().currentUser != null) {
            // If the user is logged in, navigate to the MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        setContentView(R.layout.welcome_activity_welcome)

        val navController = findNavController(R.id.nav_host_welcome)
    }
}