package it.fnorg.bellapp.login_activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
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

        // You must provide a custom layout XML resource and configure at least one
        // provider button ID. It's important that you set the button ID for every provider
        // that you have enabled.
        val customLayout = AuthMethodPickerLayout
            .Builder(R.layout.login_activity_log_in_custom)
            .setGoogleButtonId(R.id.google_button)
            .setEmailButtonId(R.id.email_button)
            .build()

        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // Create and launch sign-in intent
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setAuthMethodPickerLayout(customLayout)
            .build()

        signInLauncher.launch(signInIntent)
    }


    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser

            // TODO: Fare in modo che se l'utente si è appena registrato venga creato il documento e la raccolta
            //       relativi all'utente per sapere a che sistemi è collegato

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
                        userData = document.toObject<UserInfo>()!!
                        Log.d("LogInActivity", "User document already exists")
                    }
                    navigateToMainActivity(userData)
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
        }
    }

    private fun navigateToMainActivity(user: UserInfo) {
        // Start the MainActivity
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("UserId", user.uid)
        intent.putExtra("FullName", user.fullName)
        intent.putExtra("Email", user.email)
        startActivity(intent)
    }
}