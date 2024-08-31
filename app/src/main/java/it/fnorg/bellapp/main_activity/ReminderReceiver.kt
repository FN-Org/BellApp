package it.fnorg.bellapp.main_activity

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import it.fnorg.bellapp.R
import it.fnorg.bellapp.createNotificationChannel

/**
 * BroadcastReceiver class to handle daily reminder notifications.
 */
class ReminderReceiver : BroadcastReceiver() {

    private val CHANNELID = "Daily_reminder"
    private val channelName = "Daily Reminder Channel"
    private val channelDescriptionText = "Channel for daily reminders"

    /**
     * Called when the BroadcastReceiver receives an Intent broadcast.
     *
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received.
     */
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            Log.w("ReminderReceiver", "Intent received")
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Check if the notification channel exists for API 26+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (notificationManager.getNotificationChannel(CHANNELID) == null) {
                    createNotificationChannel(context,CHANNELID,channelName,channelDescriptionText)
                }
            }
            showNotification(context)
        }
    }




    /**
     * Shows a notification to remind the user of a task.
     *
     * @param context The Context in which the receiver is running.
     */
    private fun showNotification(context: Context) {
        val notificationId = 1

        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, CHANNELID)
            .setSmallIcon(R.mipmap.ic_bell_app)
            .setContentTitle(context.getString(R.string.reminder_title))
            .setContentText(context.getString(R.string.reminder_text))
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

            // Check for permission before showing the notification
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            with(NotificationManagerCompat.from(context)) {
                notify(notificationId, builder.build())
            }
        } else {
                Log.w("ReminderReceiver", "Notification permission not granted")
        }
    }
}
