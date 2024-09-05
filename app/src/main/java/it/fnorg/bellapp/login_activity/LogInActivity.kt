package it.fnorg.bellapp.login_activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import it.fnorg.bellapp.R
import it.fnorg.bellapp.main_activity.MainActivity
import it.fnorg.bellapp.welcome_activity.WelcomeActivity

/**
 * Activity for handling user login through Firebase Authentication.
 */
class LogInActivity : AppCompatActivity() {

    /**
     * Data class representing user information.
     *
     * @property uid User ID
     * @property fullName Full name of the user
     * @property email Email address of the user
     * @property date Timestamp of when the user information was fetched
     */
    data class UserInfo(
        val uid: String = "",
        val fullName: String = "",
        val email: String = "",
        val date: Timestamp = Timestamp.now()
    )

    // Firebase authentication result launcher
    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract(),
    ) { res ->
        this.onSignInResult(res)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Only light mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity_log_in)

        // Handle back button press to navigate to WelcomeActivity
        this.onBackPressedDispatcher.addCallback(this){
            navigateToWelcomeActivity()
        }

        // Choose authentication providers (Email and Google)
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // Create sign-in intent with FirebaseUI
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setLogo(R.mipmap.ic_bell_app)
            .setTheme(R.style.LoginTheme)
            .setTosAndPrivacyPolicyUrls("https://github.com/FN-Org/BellApp", "https://zoomquilt.org")
            .build()

        signInLauncher.launch(signInIntent)
    }

    /**
     * Callback function triggered upon Firebase Authentication result.
     *
     * @param result Result of Firebase Authentication
     */
    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser

            // Check if user is authenticated
            if (user != null) {
                val uid = user.uid
                val fullName = user.displayName ?: ""
                val email = user.email ?: ""
                val db = Firebase.firestore
                val currentUserDocRef = db.collection("users").document(uid)
                var userData : UserInfo

                // Check if user document exists in Firestore
                currentUserDocRef.get().addOnSuccessListener { document ->
                    if (!document.exists()) {
                        userData = UserInfo(uid, fullName, email, Timestamp.now())
                        currentUserDocRef.set(userData).addOnSuccessListener {
                            Log.d("LogInActivity", "User document created successfully")
                        }.addOnFailureListener { exception ->
                            Log.d("LogInActivity", "Failed to create user document: ", exception)
                        }
                    } else {
                        Log.d("LogInActivity", "User document already exists")
                    }
                    navigateToMainActivity()
                }.addOnFailureListener { exception ->
                    Log.d("LogInActivity", "Failed to check user document: ", exception)
                }
            } else {
                // No user currently authenticated
            }
        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            if (response != null) {
                Log.d("LogInActivity", "Error code: " + response.getError())
            }
            else {
                navigateToWelcomeActivity()
            }
        }
    }

    /**
     * Navigate to MainActivity.
     */
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    /**
     * Navigate to WelcomeActivity.
     */
    private fun navigateToWelcomeActivity() {
        val intent = Intent(this, WelcomeActivity::class.java)
        startActivity(intent)
    }
}