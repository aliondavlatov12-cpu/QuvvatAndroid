package tj.quvvat.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.util.Calendar

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences("quvvat_settings", Context.MODE_PRIVATE)
        val days = prefs.getStringSet("days", setOf("0", "2", "4")) ?: setOf("0", "2", "4")
        val calendarDays = listOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY)
        if (prefs.getBoolean("workout_enabled", true)) {
            days.forEach { d ->
                val i = d.toIntOrNull() ?: return@forEach
                if (i in 0..6) ReminderScheduler.scheduleWeekly(context, "workout_$i", prefs.getInt("workout_hour", 18), prefs.getInt("workout_minute", 0), calendarDays[i], "Вақти машқ расид 💪", "Қувват: вақти машқ аст. Оғоз кун!")
            }
        }
        if (prefs.getBoolean("sleep_enabled", true)) {
            ReminderScheduler.scheduleDaily(context, "sleep", prefs.getInt("sleep_hour", 22), prefs.getInt("sleep_minute", 0), "Вақти хоб расид 🌙", "Барои барқарор шудани мушакҳо вақти хоб аст.")
        }
    }
}
