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
 * Check if the connection is available
 *
 * @param   context of the activity or specific application environment
 * @return  true if it is connected, false otherwise
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
 * Open the link with a browser
 *
 * @param   context of the activity or specific application environment
 * @param   url string for the url opened
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
 * Used in MainActivity to show a warning message if you are
 * not connected to the Internet
 *
 * @param   context of the activity or specific application environment
 * @param   view    general user interface
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