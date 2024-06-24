package it.fnorg.bellapp.login_activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.firestore
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

        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build(),
        )
        // Create and launch sign-in intent
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setLogo(R.drawable.image_first_fragment)
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

                currentUserDocRef.get().addOnSuccessListener { document ->
                    if (!document.exists()) {
                        val userData = UserInfo(uid, fullName, email, Timestamp.now())
                        currentUserDocRef.set(userData).addOnSuccessListener {
                            Log.d("LogInActivity", "User document created successfully")
                        }.addOnFailureListener { exception ->
                            Log.d("LogInActivity", "Failed to create user document: ", exception)
                        }
                    } else {
                        Log.d("LogInActivity", "User document already exists")
                    }
                    navigateToMainActivity(user)
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

    private fun navigateToMainActivity(user: FirebaseUser) {
        // Start the MainActivity
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("USER", user)
        startActivity(intent)
    }
}