package it.fnorg.bellapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import it.fnorg.bellapp.main_activity.MainActivity

class BellAppFirebaseMessagingService : FirebaseMessagingService() {

    private val CHANNELID = "Firebase Notification"

    private val NOTIFICATIONID = 2


    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        message.notification.let {
            Log.d(TAG, "Message notification from ${message.from}")

            if (it != null && it.title != null && it.body != null) {
                sendFirebaseNotification(it.title!!, it.body!!)
            }
        }
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

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Check if the notification channel exists for API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(CHANNELID) == null) {
                createNotificationChannel(this)
            }
        }

        // Check for permission before showing the notification
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            with(NotificationManagerCompat.from(this)) {
                notify(NOTIFICATIONID, notificationBuilder.build())
            }
        } else {
            Log.w("BellAppFirebaseMessagingService", "Notification permission not granted")
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
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(mChannel)
        }
    }
}