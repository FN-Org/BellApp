package it.fnorg.bellapp.main_activity

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import it.fnorg.bellapp.R
import it.fnorg.bellapp.addFCMTokenToUser
import it.fnorg.bellapp.createNotificationChannel
import it.fnorg.bellapp.getSystemsIds
import it.fnorg.bellapp.main_activity.settings.dataStore
import it.fnorg.bellapp.updateFCMTokenToSystems
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class BellAppFirebaseMessagingService : FirebaseMessagingService() {

    // Constants for notification settings
    val CHANNELID = "Firebase Notification"
    private val channelName = "Firebase cloud messaging"
    private val channelDescription = "Firebase cloud messaging notification after event execution"
    private val NOTIFICATIONID = 2
    private val EVENT_NOTIFICATION = booleanPreferencesKey("event_notification")

    // Coroutine scope for managing asynchronous tasks
    private val scope = CoroutineScope(NonCancellable)

    private val TAG = "BellAppFirebaseMessagingService"

    /**
     * Called when the FCM token is updated.
     * Registers the token to the user and updates the systems with the new token.
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        addFCMTokenToUser(token)
        getSystemsIds() { systemsId ->
           updateFCMTokenToSystems(token,systemsId)
        }
    }

    /**
     * Handles incoming messages from Firebase Cloud Messaging.
     * If a notification is included, it checks the user's preferences and may send a local notification.
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d(TAG, "Message from ${message.from}")

        message.notification?.let { notification ->
            Log.d(TAG, "Message notification from ${message.from}")

            scope.launch {
                try {
                    // Check if event notifications are enabled in user preferences
                    val eventNotificationEnabled = this@BellAppFirebaseMessagingService.dataStore.data
                        .map { settings ->
                            settings[EVENT_NOTIFICATION] == true
                        }
                        .first()

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

    /**
     * Called when the service is destroyed.
     * Cancels the CoroutineScope to avoid memory leaks.
     */
    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    /**
     * Sends a notification using Firebase message data.
     * The notification includes an intent to open the main activity when clicked.
     */
    private fun sendFirebaseNotification(title: String, messageBody: String){
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        // Creates a PendingIntent to open the MainActivity when the notification is clicked
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

        // Build the notification
        val notificationBuilder = NotificationCompat.Builder(this, CHANNELID)
            .setSmallIcon(R.mipmap.ic_bell_app)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        // Get the NotificationManager system service
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Check if the notification channel exists for API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(CHANNELID) == null) {
                createNotificationChannel(this,CHANNELID,channelName,channelDescription)
            }
        }

        // Check for notification permission before displaying the notification
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            with(NotificationManagerCompat.from(this)) {
                notify(NOTIFICATIONID, notificationBuilder.build())
            }
        } else {
            Log.w(TAG, "Notification permission not granted")
        }

    }


}