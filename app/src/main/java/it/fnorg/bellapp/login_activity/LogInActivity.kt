package it.fnorg.bellapp.login_activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import it.fnorg.bellapp.R
import it.fnorg.bellapp.main_activity.MainActivity
import it.fnorg.bellapp.welcome_activity.WelcomeActivity

class LogInActivity : AppCompatActivity() {

    data class UserInfo(
        val uid: String = "",
        val fullName: String = "",
        val email: String = "",
        val date: Timestamp = Timestamp.now()
    )

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract(),
    ) { res ->
        this.onSignInResult(res)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity_log_in)

        this.onBackPressedDispatcher.addCallback(this){
            navigateToWelcomeActivity()
        }

        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // Create and launch sign-in intent
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setLogo(R.mipmap.ic_bell_app)
            .setTheme(R.style.LoginTheme)
            .setTosAndPrivacyPolicyUrls("https://github.com/FN-Org/BellApp", "https://zoomquilt.org")
            .build()

        signInLauncher.launch(signInIntent)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser

            // Controlla se l'utente è autenticato
            if (user != null) {
                val uid = user.uid
                val fullName = user.displayName ?: ""
                val email = user.email ?: ""
                val db = Firebase.firestore
                val currentUserDocRef = db.collection("users").document(uid)
                var userData : UserInfo

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
                // Nessun utente attualmente autenticato
            }
        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            // ...
            if (response != null) {
                Log.d("LogInActivity", "Error code: "+ response.getError())
            }
            else {
                navigateToWelcomeActivity()
            }
        }
    }

    private fun navigateToMainActivity() {
        // Start the MainActivity
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToWelcomeActivity() {
        // Start the MainActivity
        val intent = Intent(this, WelcomeActivity::class.java)
        startActivity(intent)
    }
}