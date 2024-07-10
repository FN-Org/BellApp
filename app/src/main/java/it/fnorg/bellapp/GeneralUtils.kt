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

fun openLink(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse(url)
    // Controlla se c'è un'attività che può gestire l'intento
    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
    // Gestisci il caso in cui nessun browser è disponibile per aprire il link
    // Ad esempio, mostra un messaggio all'utente
    }
}

fun checkConnection(context: Context, view: View) {
    val warningMessage: LinearLayout? = view.findViewById(R.id.connection_warning)
    if (!isInternetAvailable(context)) {
        if (warningMessage != null) {
            warningMessage.visibility = View.VISIBLE
            Log.w("Check Connection", "Belin ha funzionato")
        }
        else {
            Log.w("Check Connection", "Non funziona un belino, linear layout nullo")
        }
    }
    else {
        if (warningMessage != null) {
            warningMessage.visibility = View.GONE
            Log.w("Check Connection", "Belin ha funzionato")
        }
        else {
            Log.w("Check Connection", "Non funziona un belino, linear layout nullo")
        }
    }
}