package it.fnorg.bellapp

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast

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
            Log.w("Check Connection", "Warning message is null")
        }
    }
    else {
        if (warningMessage != null) {
            warningMessage.visibility = View.GONE
        }
        else {
            Log.w("Check Connection", "Warning message is null")
        }
    }
}