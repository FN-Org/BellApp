package it.fnorg.bellapp.main_activity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import it.fnorg.bellapp.R

class ReminderReceiver : BroadcastReceiver() {

    private val CHANNELID = "Daily_reminder"

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {

            Log.w("ReminderReceiver","Received the intent")
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            // Mostra la notifica o avvia un'altra azione desiderata
            if (notificationManager.getNotificationChannel(CHANNELID) == null) {
               createNotificationChannel(context)
              }
            showNotification(context)
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Daily Reminder Channel"
            val descriptionText = "Channel for daily reminders"
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

    private fun showNotification(context: Context) {
        //createNotificationChannel(context)

        val notificationId = 1

        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent,  PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, CHANNELID)
            .setSmallIcon(R.mipmap.ic_bell_app)
            .setContentTitle(context.getString(R.string.reminder_title))
            .setContentText(context.getString(R.string.reminder_text))
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }
}
