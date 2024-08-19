package it.fnorg.bellapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import it.fnorg.bellapp.main_activity.MainActivity
import it.fnorg.bellapp.main_activity.settings.dataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch



class BellAppFirebaseMessagingService : FirebaseMessagingService() {

    private val CHANNELID = "Firebase Notification"
    private val NOTIFICATIONID = 2
    private val EVENT_NOTIFICATION = booleanPreferencesKey("event_notification")

    private val scope = CoroutineScope(Dispatchers.IO + Job()) // Crea un CoroutineScope dedicato per il servizio

    private val TAG = "BellAppFirebaseMessagingService"





    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        addFCMTokenToUser(token)
    }


    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        message.notification?.let { notification -> // Usare ?.let per evitare null checks multipli
            Log.d(TAG, "Message notification from ${message.from}")

            scope.launch {
                try {
                    val eventNotificationEnabled = this@BellAppFirebaseMessagingService.dataStore.data
                        .map { settings ->
                            settings[EVENT_NOTIFICATION] == true
                        }
                        .first() // Colleziona il primo valore emesso dal Flow

                    if (eventNotificationEnabled) {
                        Log.w(TAG, "Events notification enabled. Sending notification.")
                        sendFirebaseNotification(notification.title!!, notification.body!!)
                    } else {
                        Log.w(TAG, "Events notification not enabled.")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error checking notification settings: ${e.message}", e)
                }
            }
        } ?: run {
            Log.w(TAG, "Notification is null or missing required fields")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel() // Cancella il CoroutineScope quando il servizio viene distrutto
    }

    private fun sendFirebaseNotification(title: String, messageBody: String){
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)


        val notificationBuilder = NotificationCompat.Builder(this, CHANNELID)
            .setSmallIcon(R.mipmap.ic_bell_app)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Check if the notification channel exists for API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(CHANNELID) == null) {
                createNotificationChannel(this)
            }
        }

        // Check for permission before showing the notification
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            with(NotificationManagerCompat.from(this)) {
                notify(NOTIFICATIONID, notificationBuilder.build())
            }
        } else {
            Log.w(TAG, "Notification permission not granted")
        }

    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Firebase Notification"
            val descriptionText = "Channel for notification from firebase cloud messaging"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(
                CHANNELID,
                name,
                importance
            )
            mChannel.description = descriptionText

            val notificationManager =
                context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(mChannel)
        }
    }

}