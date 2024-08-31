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
import androidx.fragment.app.activityViewModels
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

    val CHANNELID = "Firebase Notification"
    private val channelName = "Firebase cloud messaging"
    private val channelDescription = "Firebase cloud messaging notification after event execution"
    private val NOTIFICATIONID = 2
    private val EVENT_NOTIFICATION = booleanPreferencesKey("event_notification")

    private val scope = CoroutineScope(NonCancellable) // Crea un CoroutineScope dedicato per il servizio

    private val TAG = "BellAppFirebaseMessagingService"





    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        addFCMTokenToUser(token)
        getSystemsIds() { systemsId ->
           updateFCMTokenToSystems(token,systemsId)
        }
    }


    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d(TAG, "Message from ${message.from}")

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
                createNotificationChannel(this,CHANNELID,channelName,channelDescription)
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


}