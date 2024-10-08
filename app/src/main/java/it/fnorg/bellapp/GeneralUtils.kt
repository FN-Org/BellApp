package it.fnorg.bellapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessagingService.NOTIFICATION_SERVICE

/**
 * Checks if the internet connection is available.
 *
 * @param context the context of the activity or specific application environment
 * @return true if the device is connected to the internet, false otherwise
 */
fun isInternetAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = connectivityManager.activeNetwork ?: return false
    val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
    return when {
        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
        else -> false
    }
}

/**
 * Opens a URL link in a browser.
 *
 * @param context the context of the activity or specific application environment
 * @param url the URL to be opened in the browser
 */
fun openLink(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse(url)
    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        Toast.makeText(context, context.getString(R.string.sww_try_again), Toast.LENGTH_SHORT).show()
    }
}

/**
 * Checks the internet connection and shows a warning message if not connected.
 * Used in MainActivity to display a warning message when there is no internet connection.
 *
 * @param context the context of the activity or specific application environment
 * @param view the root view containing the user interface elements
 */
fun checkConnection(context: Context, view: View) {
    val warningMessage: LinearLayout? = view.findViewById(R.id.connection_warning)
    if (!isInternetAvailable(context)) {
        if (warningMessage != null) {
            warningMessage.visibility = View.VISIBLE
        }
        else {
            Log.w("General Utils", "Warning message is null")
        }
    }
    else {
        if (warningMessage != null) {
            warningMessage.visibility = View.GONE
        }
        else {
            Log.w("General Utils", "Warning message is null")
        }
    }
}

/**
 * Adds the given FCM (Firebase Cloud Messaging) token to the current user in the Firestore database.
 *
 * @param token The FCM token to be added to the user's Firestore document.
 */
fun addFCMTokenToUser(token: String){
    val db = Firebase.firestore
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    if (uid != null){
        db.collection("users")
            .document(uid)
            .update("fcmToken",token)
            .addOnSuccessListener {
                Log.w("General Utils", "Refreshed token: $token added to firebase")
            }
            .addOnFailureListener{
                Log.w("General Utils", "Something went wrong with token $token")
            }
    }
}

/**
 * Updates the FCM token in all the system documents associated with the given system IDs.
 *
 * @param token The FCM token to be updated.
 * @param systemsId List of system IDs where the token will be updated.
 */
fun updateFCMTokenToSystems(token: String, systemsId:List<String>) {
    val db = Firebase.firestore
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    if (uid != null) {
        for (id in systemsId) {
            db.collection("systems")
                .document(id)
                .collection("tokensFCM")
                .document(uid)
                .set(mapOf("token" to token))
                .addOnSuccessListener {
                    Log.d(TAG, "Document updated with ID: $id")
                }
                .addOnFailureListener{
                    Log.d(TAG, "Document NOT updated with ID: $id")
                }

        }
    }
}

/**
 * Removes the current user's FCM token from the specified system document in Firestore.
 *
 * @param sysId The system ID from which the token will be removed.
 */
fun removeFCMTokenFromSystem(sysId: String){
    val db = Firebase.firestore
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    if (uid != null) {
        db.collection("systems")
            .document(sysId)
            .collection("tokensFCM")
            .document(uid)
            .delete()
    }
}

/**
 * Retrieves the list of system IDs associated with the current user and returns them through a callback.
 *
 * @param onComplete A callback function that receives a list of system IDs once retrieved.
 */
fun getSystemsIds(onComplete: (List<String>) -> Unit) {
    val db = Firebase.firestore
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    if (uid != null) {
        db.collection("users")
            .document(uid)
            .collection("systems")
            .get()
            .addOnSuccessListener { documents ->
                val systemsId = documents.map { it.id }
                onComplete(systemsId)
            }
            .addOnFailureListener { exception ->
                Log.e(
                    "General Utils",
                    "Error fetching ID: ${exception.message}")
                onComplete(emptyList())
            }
    }
}

/**
 * Creates a notification channel for devices running Android O or higher.
 *
 * @param context The Context in which the receiver is running.
 * @param channelId The id of the channel
 * @param name The name of the channel
 * @param descriptionText The description text of the channel
 */
fun createNotificationChannel(context: Context,channelId:String,name:String,descriptionText:String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val importance = NotificationManager.IMPORTANCE_HIGH
        val mChannel = NotificationChannel(
            channelId,
            name,
            importance
        )
        mChannel.description = descriptionText

        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.createNotificationChannel(mChannel)
    }
}
