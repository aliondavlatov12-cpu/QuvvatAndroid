package tj.quvvat.app

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val key = intent.getStringExtra("key") ?: "reminder"
        val title = intent.getStringExtra("title") ?: "Қувват"
        val message = intent.getStringExtra("message") ?: "Вақти ёдраскунист."
        val prefs = context.getSharedPreferences("quvvat_settings", Context.MODE_PRIVATE)
        val showNotification = prefs.getBoolean("notification_enabled", true)
        val ringAlarm = prefs.getBoolean("alarm_enabled", true)
        if (showNotification || ringAlarm) {
            val manager = context.getSystemService(NotificationManager::class.java)
            val channelId = if (ringAlarm) "quvvat_alarm" else "quvvat_reminders"
            if (Build.VERSION.SDK_INT >= 26) {
                val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                val attrs = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
                val channel = NotificationChannel(channelId, if (ringAlarm) "Будильники Қувват" else "Ёдраскуниҳои Қувват",
                    if (ringAlarm) NotificationManager.IMPORTANCE_HIGH else NotificationManager.IMPORTANCE_DEFAULT)
                if (ringAlarm) {
                    channel.setSound(sound, attrs)
                    channel.enableVibration(true)
                    channel.vibrationPattern = longArrayOf(0, 700, 300, 700, 300, 1000)
                }
                manager.createNotificationChannel(channel)
            }
            val open = PendingIntent.getActivity(context, 9001, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(title).setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setContentIntent(open).setAutoCancel(true)
                .setPriority(if (ringAlarm) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(if (ringAlarm) NotificationCompat.CATEGORY_ALARM else NotificationCompat.CATEGORY_REMINDER)
                .setDefaults(if (ringAlarm) NotificationCompat.DEFAULT_ALL else NotificationCompat.DEFAULT_LIGHTS)
                .build()
            manager.notify(key.hashCode(), notification)
        }
        val interval = intent.getLongExtra("interval", 0L)
        if (interval > 0) {
            val next = Intent(context, ReminderReceiver::class.java).apply {
                putExtra("key", key); putExtra("title", title); putExtra("message", message); putExtra("interval", interval)
            }
            val pi = PendingIntent.getBroadcast(context, key.hashCode(), next, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val am = context.getSystemService(AlarmManager::class.java)
            try { am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval, pi) }
            catch (_: SecurityException) { am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval, pi) }
        }
    }
}
