package tj.quvvat.app

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.*
import java.util.Calendar

class MainActivity : Activity() {
    private lateinit var workoutTime: TimePicker
    private lateinit var sleepTime: TimePicker
    private lateinit var workoutEnabled: CheckBox
    private lateinit var sleepEnabled: CheckBox
    private lateinit var notifEnabled: CheckBox
    private lateinit var alarmEnabled: CheckBox
    private lateinit var days: List<CheckBox>
    private lateinit var status: TextView
    private val prefs by lazy { getSharedPreferences("quvvat_settings", MODE_PRIVATE) }
    private val dayNames = listOf("Душанбе", "Сешанбе", "Чоршанбе", "Панҷшанбе", "Ҷумъа", "Шанбе", "Якшанбе")
    private val dayCalendar = listOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scroll = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 22, 24, 28)
            setBackgroundColor(0xFFF4F7F5.toInt())
        }
        scroll.addView(layout)
        setContentView(scroll)

        layout.addView(TextView(this).apply {
            text = "💪 Қувват"
            textSize = 30f; setTextColor(0xFF0D3B2A.toInt()); gravity = Gravity.CENTER
        })
        layout.addView(TextView(this).apply {
            text = "Нақшаи машқ, хоб ва ёдраскуниҳо"
            textSize = 16f; gravity = Gravity.CENTER; setPadding(0, 8, 0, 20)
        })

        workoutEnabled = check("Ёдрас кардани вақти машқ", prefs.getBoolean("workout_enabled", true), layout)
        addLabel(layout, "Вақти машқ")
        workoutTime = TimePicker(this).apply { setIs24HourView(true); hour = prefs.getInt("workout_hour", 18); minute = prefs.getInt("workout_minute", 0) }
        layout.addView(workoutTime, matchWrap())
        sleepEnabled = check("Ёдрас кардани вақти хоб", prefs.getBoolean("sleep_enabled", true), layout)
        addLabel(layout, "Вақти хоб")
        sleepTime = TimePicker(this).apply { setIs24HourView(true); hour = prefs.getInt("sleep_hour", 22); minute = prefs.getInt("sleep_minute", 0) }
        layout.addView(sleepTime, matchWrap())

        addLabel(layout, "Рӯзҳои машқ")
        val savedDays = prefs.getStringSet("days", setOf("0", "2", "4")) ?: setOf("0", "2", "4")
        days = dayNames.mapIndexed { index, name ->
            CheckBox(this).apply {
                text = name
                isChecked = savedDays.contains(index.toString())
                textSize = 16f
                layout.addView(this)
            }
        }

        addLabel(layout, "Навъи ёдраскунӣ")
        notifEnabled = check("Уведомление", prefs.getBoolean("notification_enabled", true), layout)
        alarmEnabled = check("Будильник бо садои баланд", prefs.getBoolean("alarm_enabled", true), layout)
        layout.addView(TextView(this).apply {
            text = "Будильник истифода мебарад садо ва ларзиши телефон. Баландии садоро дар танзимоти садои телефон зиёд кунед."
            textSize = 13f; setTextColor(0xFF4C5A52.toInt()); setPadding(0, 2, 0, 12)
        })

        val save = Button(this).apply {
            text = "💾 Захира ва фаъол кардан"
            setOnClickListener { saveAndSchedule() }
        }
        layout.addView(save, matchWrap())
        val permissions = Button(this).apply {
            text = "⚙️ Иҷозатҳои Android"
            setOnClickListener { requestPermissionsAndSettings() }
        }
        layout.addView(permissions, matchWrap())
        val test = Button(this).apply {
            text = "🔔 Санҷидани ёдраскуниҳо (10 сония)"
            setOnClickListener { scheduleTest() }
        }
        layout.addView(test, matchWrap())

        status = TextView(this).apply { textSize = 14f; setPadding(0, 16, 0, 4); setTextColor(0xFF0D3B2A.toInt()) }
        layout.addView(status)
        layout.addView(TextView(this).apply {
            text = "Иловаҳо: интихоб кардани рӯзҳо, ҷадвали машқ ва хоб, санҷиши ёдраскунӣ, нигоҳ доштани танзимот ва барқарор кардани ёдраскуниҳо баъди бозоғозии телефон.\n\nМуҳим: дар баъзе телефонҳо барои кори боэътимод ҳангоми баста будани барнома, иҷозати Exact alarms ва иҷозати кор дар пасзамина лозим мешавад."
            textSize = 13f; setTextColor(0xFF4C5A52.toInt()); setPadding(0, 12, 0, 12)
        })
        status.text = permissionStatus()
    }

    private fun check(label: String, checked: Boolean, parent: LinearLayout): CheckBox {
        return CheckBox(this).apply { text = label; isChecked = checked; textSize = 16f; parent.addView(this) }
    }
    private fun addLabel(parent: LinearLayout, text: String) {
        parent.addView(TextView(this).apply {
            this.text = text; textSize = 19f; setTextColor(0xFF0D3B2A.toInt()); setPadding(0, 18, 0, 4)
        })
    }
    private fun matchWrap() = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

    private fun saveAndSchedule() {
        val selected = days.mapIndexedNotNull { i, cb -> if (cb.isChecked) i.toString() else null }.toSet()
        prefs.edit()
            .putBoolean("workout_enabled", workoutEnabled.isChecked)
            .putInt("workout_hour", workoutTime.hour).putInt("workout_minute", workoutTime.minute)
            .putBoolean("sleep_enabled", sleepEnabled.isChecked)
            .putInt("sleep_hour", sleepTime.hour).putInt("sleep_minute", sleepTime.minute)
            .putStringSet("days", selected)
            .putBoolean("notification_enabled", notifEnabled.isChecked)
            .putBoolean("alarm_enabled", alarmEnabled.isChecked)
            .apply()
        ReminderScheduler.cancelAll(this)
        if (workoutEnabled.isChecked) {
            selected.forEach { day ->
                ReminderScheduler.scheduleWeekly(this, "workout_${day}", workoutTime.hour, workoutTime.minute, dayCalendar[day.toInt()], "Вақти машқ расид 💪", "Қувват: вақти машқ аст. Оғоз кун!")
            }
        }
        if (sleepEnabled.isChecked) {
            // Sleep reminder is daily.
            ReminderScheduler.scheduleDaily(this, "sleep", sleepTime.hour, sleepTime.minute, "Вақти хоб расид 🌙", "Барои барқарор шудани мушакҳо вақти хоб аст.")
        }
        status.text = "Танзимот захира шуд. ${permissionStatus()}"
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
        }
        if (Build.VERSION.SDK_INT >= 31) {
            val am = getSystemService(AlarmManager::class.java)
            if (!am.canScheduleExactAlarms()) {
                startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName")))
            }
        }
    }

    private fun scheduleTest() {
        ReminderScheduler.scheduleOnce(this, "test", System.currentTimeMillis() + 10_000L, "Санҷиши Қувват 🔔", "Ёдраскунии Қувват кор мекунад.")
        status.text = "Ёдраскунии санҷишӣ баъди 10 сония меояд. ${permissionStatus()}"
    }

    private fun requestPermissionsAndSettings() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
        }
        if (Build.VERSION.SDK_INT >= 31 && !getSystemService(AlarmManager::class.java).canScheduleExactAlarms()) {
            startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName")))
        } else {
            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")))
        }
    }

    private fun permissionStatus(): String {
        val notifications = if (Build.VERSION.SDK_INT < 33 || checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) "огоҳинома: иҷозат ҳаст" else "огоҳинома: иҷозат лозим"
        val exact = if (Build.VERSION.SDK_INT < 31 || getSystemService(AlarmManager::class.java).canScheduleExactAlarms()) "будильник: иҷозат ҳаст" else "будильник: иҷозати Exact alarms лозим"
        return "$notifications • $exact"
    }
}

object ReminderScheduler {
    fun scheduleWeekly(context: Context, key: String, hour: Int, minute: Int, dayOfWeek: Int, title: String, message: String) {
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour); set(Calendar.MINUTE, minute); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            while (get(Calendar.DAY_OF_WEEK) != dayOfWeek || timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        scheduleRepeating(context, key, next.timeInMillis, 7 * 24 * 60 * 60 * 1000L, title, message)
    }
    fun scheduleDaily(context: Context, key: String, hour: Int, minute: Int, title: String, message: String) {
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour); set(Calendar.MINUTE, minute); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        scheduleRepeating(context, key, next.timeInMillis, 24 * 60 * 60 * 1000L, title, message)
    }
    fun scheduleOnce(context: Context, key: String, time: Long, title: String, message: String) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("key", key); putExtra("title", title); putExtra("message", message)
        }
        val pi = PendingIntent.getBroadcast(context, key.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val am = context.getSystemService(AlarmManager::class.java)
        try { am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pi) }
        catch (_: SecurityException) { am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pi) }
    }
    private fun scheduleRepeating(context: Context, key: String, time: Long, interval: Long, title: String, message: String) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("key", key); putExtra("title", title); putExtra("message", message); putExtra("interval", interval)
        }
        val pi = PendingIntent.getBroadcast(context, key.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val am = context.getSystemService(AlarmManager::class.java)
        try { am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pi) }
        catch (_: SecurityException) { am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pi) }
    }
    fun cancelAll(context: Context) {
        val prefs = context.getSharedPreferences("quvvat_settings", Context.MODE_PRIVATE)
        val am = context.getSystemService(AlarmManager::class.java)
        val keys = mutableListOf<String>()
        for (i in 0..6) keys.add("workout_$i")
        keys.addAll(listOf("sleep", "test"))
        keys.forEach { key ->
            val intent = Intent(context, ReminderReceiver::class.java)
            val pi = PendingIntent.getBroadcast(context, key.hashCode(), intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
            if (pi != null) am.cancel(pi)
        }
    }
}
