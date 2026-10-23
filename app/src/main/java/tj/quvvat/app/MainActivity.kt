package tj.quvvat.app

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.*
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import java.util.Calendar

private const val INK = 0xFF17231F.toInt()
private const val MUTED = 0xFF66736D.toInt()
private const val GREEN = 0xFF176B4D.toInt()
private const val PALE = 0xFFF2F6F3.toInt()
private const val WHITE = 0xFFFFFFFF.toInt()

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("quvvat_settings", MODE_PRIVATE) }
    private lateinit var content: LinearLayout
    private lateinit var nav: LinearLayout
    private val dayNames = listOf("Душанбе", "Сешанбе", "Чоршанбе", "Панҷшанбе", "Ҷумъа", "Шанбе", "Якшанбе")
    private val dayCalendar = listOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY)
    private data class Exercise(val name:String, val target:String, val detail:String, val tip:String, val group:String)
    private val plan = listOf(
        Exercise("Гармкунӣ", "5 дақиқа", "Дар ҷой роҳравӣ, давр задани дасту китф ва ҳаракатҳои сабук.", "Пеш аз ҳар машқ иҷро кун; ҳаракатҳоро бе шитоб анҷом деҳ.", "Омодагӣ"),
        Exercise("Отжимание", "3 × 8–12", "Баданро рост нигоҳ дор, оринҷҳоро хам карда қафаси синаро ба замин наздик кун ва боло баргард.", "Агар душвор бошад, аз зону иҷро кун. Ҳангоми боло рафтан нафас барор.", "Сина • китф • трицепс"),
        Exercise("Приседание", "3 × 12–15", "Пойҳо тақрибан баробари китф. Пуштро рост нигоҳ дошта, косро ба қафо бар ва нишин.", "Зонуҳо ба самти ангуштони пой бошанд; пошна аз замин наканад.", "Пойҳо"),
        Exercise("Кашидан аз турник", "3 × 3–8", "Аз турник овезон шав, китфҳоро идора карда, баданро боло каш ва оҳиста поён фаро.", "Агар ҳоло натавонӣ, овезон мондан ё кашиши ёрирасонро интихоб кун. Бе ҷунбиш.", "Пушт • дастҳо"),
        Exercise("Планка", "3 × 20–40 сония", "Ба оринҷҳо такя кун. Сар, пушт ва кос дар як хат бошанд.", "Шикамро каме таранг нигоҳ дор; агар миён дард кунад, қатъ кун.", "Шикам"),
        Exercise("Ланҷ", "2 × 8 ҳар пой", "Як қадам ба пеш гузор, ҳар ду зонуро хам кун ва ба ҳолати оғоз баргард.", "Қадамро устувор гузор ва зонуи пешро ба дарун наафтон.", "Пойҳо • мувозинат"),
        Exercise("Глют-бридж", "3 × 12", "Ба пушт хобида зонуҳоро хам кун. Косро боло бардор, лаҳзае нигоҳ дор ва оҳиста фарор.", "Аз пошнаҳо такя кун ва миёнро аз ҳад зиёд хам накун.", "Пушти кос • миён"),
        Exercise("Bird-dog", "2 × 10 ҳар тараф", "Дар чор такягоҳ истода, дасти муқобилу пойро дароз кун, баъд тарафро иваз намо.", "Косро устувор нигоҳ дор; ҳаракатро назорат кун.", "Маркази бадан"),
        Exercise("Оромкунӣ ва кашиш", "4–5 дақиқа", "Роҳравии ором ва кашиши сабуки мушакҳои пой, сина ва пушт.", "Кашиш бояд сабук бошад, на дарднок.", "Барқароршавӣ")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = PALE; window.navigationBarColor = PALE
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(PALE) }
        content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(18), dp(12), dp(18), dp(20)) }
        val scroll = ScrollView(this).apply { fillViewport = true; addView(content) }
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        nav = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER; setPadding(dp(6), dp(8), dp(6), dp(8)); setBackgroundColor(WHITE) }
        root.addView(nav, LinearLayout.LayoutParams(-1, dp(66)))
        setContentView(root)
        showHome()
    }

    private fun dp(v:Int)= (v*resources.displayMetrics.density).toInt()
    private fun column() = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL }
    private fun row() = LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL }
    private fun text(s:String, size:Float=15f, color:Int=INK, bold:Boolean=false):TextView = TextView(this).apply { text=s; textSize=size; setTextColor(color); if(bold) setTypeface(null,Typeface.BOLD) }
    private fun space(h:Int) { content.addView(View(this), LinearLayout.LayoutParams(1,dp(h))) }
    private fun card():LinearLayout = column().apply { setPadding(dp(16),dp(15),dp(16),dp(15)); background=shape(WHITE,18); elevation=dp(1).toFloat() }
    private fun shape(color:Int, radius:Int):GradientDrawable = GradientDrawable().apply { setColor(color); cornerRadius=dp(radius).toFloat() }
    private fun button(label:String, action:()->Unit):Button = Button(this).apply { text=label; textSize=14f; isAllCaps=false; setTextColor(WHITE); background=shape(GREEN,14); setPadding(dp(12),dp(4),dp(12),dp(4)); setOnClickListener { action() } }
    private fun header(title:String, subtitle:String) { content.removeAllViews(); content.addView(text("ҚУВВАТ  •  ФИТНЕС",12f,GREEN,true)); space(5); content.addView(text(title,29f,INK,true)); space(4); content.addView(text(subtitle,14f,MUTED)); space(18); buildNav() }
    private fun buildNav(selected:String) {
        nav.removeAllViews()
        listOf("Имрӯз" to "home", "Машқҳо" to "workouts", "Нақша" to "plan", "Танзимот" to "settings").forEach { (label,key) ->
            val b=TextView(this).apply { text=(if(key==selected) "●  " else "")+label; textSize=12f; gravity=Gravity.CENTER; setTextColor(if(key==selected) GREEN else MUTED); setTypeface(null,if(key==selected) Typeface.BOLD else Typeface.NORMAL); setPadding(dp(5),dp(8),dp(5),dp(8)); setOnClickListener { when(key){"home"->showHome();"workouts"->showWorkouts();"plan"->showPlan();else->showSettings()} } }
            nav.addView(b,LinearLayout.LayoutParams(0,-1,1f))
        }
    }
    private fun showHome() {
        header("Салом! 💪", "Қадами хурд имрӯз, қуввати бештар фардо.")
        val c=card(); c.addView(text("МАШҚИ ИМРӮЗ",12f,GREEN,true)); spaceIn(c,6); c.addView(text(if(isWorkoutDay()) "Имрӯз рӯзи машқ аст" else "Имрӯз барқарор шав",21f,INK,true)); c.addView(text(if(isWorkoutDay()) "Нақшаи пурра • тақрибан 30–40 дақиқа" else "Истироҳат ҳам қисми пешрафт аст. Агар хоҳӣ, машқи сабук кун.",14f,MUTED)); spaceIn(c,14); c.addView(button(if(isWorkoutDay()) "Нақшаи машқро кушо  →" else "Дидани машқҳо  →") { showWorkouts() }, LinearLayout.LayoutParams(-1,dp(48))); content.addView(c); space(14)
        val stats=row(); val s1=card(); s1.addView(text("9",27f,GREEN,true)); s1.addView(text("машқи дастрас",12f,MUTED)); val s2=card(); s2.addView(text("3 рӯз",27f,GREEN,true)); s2.addView(text("нақшаи ҳафта",12f,MUTED)); stats.addView(s1,LinearLayout.LayoutParams(0,-2,1f)); stats.addView(spaceView(10),LinearLayout.LayoutParams(dp(10),1)); stats.addView(s2,LinearLayout.LayoutParams(0,-2,1f)); content.addView(stats); space(14)
        val quick=card(); quick.addView(text("Зуд дастрас",18f,INK,true)); spaceIn(quick,8); quick.addView(button("📋  Ҳамаи машқҳо") { showWorkouts() },LinearLayout.LayoutParams(-1,dp(46))); spaceIn(quick,8); quick.addView(button("📅  Рӯзҳои ҳафтаро интихоб кун") { showPlan() },LinearLayout.LayoutParams(-1,dp(46))); spaceIn(quick,8); quick.addView(button("⏰  Вақти машқ ва хоб") { showSettings() },LinearLayout.LayoutParams(-1,dp(46))); content.addView(quick)
    }
    private fun spaceIn(parent:LinearLayout,h:Int){parent.addView(View(this),LinearLayout.LayoutParams(1,dp(h)))}
    private fun spaceView(h:Int)=View(this)
    private fun isWorkoutDay():Boolean { val today=(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)+5)%7; val set=prefs.getStringSet("days",setOf("0","2","4"))?:setOf("0","2","4"); return set.contains(today.toString()) }
    private fun showWorkouts() {
        header("Машқҳо", "Ҳар машқро кушо, то тарзи дурусти иҷро ва миқдорро бинӣ.")
        val notice=card(); notice.addView(text("Нақшаи пурраи хонагӣ",18f,INK,true)); notice.addView(text("Таҷҳизот: вазни бадан ва турник. Байни сетҳо 60–90 сония истироҳат кун.",14f,MUTED)); content.addView(notice); space(12)
        plan.forEachIndexed { i,e -> val c=card(); val r=row(); val num=TextView(this).apply { text=String.format("%02d",i+1); textSize=14f; setTextColor(GREEN); setTypeface(null,Typeface.BOLD); gravity=Gravity.CENTER; background=shape(0xFFE2F1E8.toInt(),12); setPadding(dp(12),dp(12),dp(12),dp(12)) }; r.addView(num); val info=column().apply { setPadding(dp(12),0,0,0); addView(text(e.name,17f,INK,true)); addView(text(e.target,13f,GREEN,true)); addView(text(e.group,12f,MUTED)) }; r.addView(info,LinearLayout.LayoutParams(0,-2,1f)); val open=TextView(this).apply { text="Дидан  ›"; textSize=12f; setTextColor(GREEN); setOnClickListener { showExercise(e) } }; r.addView(open); c.addView(r); c.setOnClickListener { showExercise(e) }; content.addView(c); space(8) }
    }
    private fun showExercise(e:Exercise) {
        header(e.name, e.group)
        val c=card(); c.addView(text("Миқдор",12f,GREEN,true)); spaceIn(c,4); c.addView(text(e.target,24f,INK,true)); spaceIn(c,16); c.addView(text("Тарзи иҷро",17f,INK,true)); spaceIn(c,5); c.addView(text(e.detail,15f,MUTED)); spaceIn(c,14); c.addView(text("Маслиҳат",17f,INK,true)); spaceIn(c,5); c.addView(text(e.tip,15f,MUTED)); content.addView(c); space(12); content.addView(button("← Бозгашт ба ҳамаи машқҳо") { showWorkouts() },LinearLayout.LayoutParams(-1,dp(48)))
    }
    private fun showPlan() {
        header("Нақшаи ҳафта", "Рӯзҳоеро интихоб кун, ки воқеан вақт дорӣ.")
        val saved=prefs.getStringSet("days",setOf("0","2","4"))?:setOf("0","2","4"); val checks=mutableListOf<CheckBox>()
        val c=card(); c.addView(text("Рӯзҳои машқ",18f,INK,true)); spaceIn(c,8)
        dayNames.forEachIndexed { i,n -> val cb=CheckBox(this).apply { text=n; textSize=15f; isChecked=saved.contains(i.toString()); buttonTintList=android.content.res.ColorStateList.valueOf(GREEN); setPadding(0,dp(4),0,dp(4)) }; checks.add(cb); c.addView(cb) }
        spaceIn(c,8); c.addView(text("Пешниҳод: 3 рӯз машқ ва рӯзҳои байни онҳо истироҳат. Агар хастагӣ ё дард бошад, истироҳат кун.",13f,MUTED)); content.addView(c); space(12); content.addView(button("Захира кардани рӯзҳо") { prefs.edit().putStringSet("days",checks.mapIndexedNotNull{ i,cb->if(cb.isChecked)i.toString() else null}.toSet()).apply(); Toast.makeText(this,"Нақша захира шуд",Toast.LENGTH_SHORT).show(); showPlan() },LinearLayout.LayoutParams(-1,dp(50)))
    }
    private fun showSettings() {
        header("Танзимот", "Вақтҳои ёдраскуниро интихоб ва фаъол кун.")
        val c=card(); val work=CheckBox(this).apply { text="Ёдраскунии вақти машқ"; isChecked=prefs.getBoolean("workout_enabled",true); textSize=15f }; c.addView(work); c.addView(text("Вақти машқ",13f,MUTED)); val wt=TimePicker(this).apply { setIs24HourView(true); hour=prefs.getInt("workout_hour",18); minute=prefs.getInt("workout_minute",0) }; c.addView(wt,LinearLayout.LayoutParams(-1,dp(150))); spaceIn(c,10)
        val sleep=CheckBox(this).apply { text="Ёдраскунии вақти хоб"; isChecked=prefs.getBoolean("sleep_enabled",true); textSize=15f }; c.addView(sleep); c.addView(text("Вақти хоб",13f,MUTED)); val st=TimePicker(this).apply { setIs24HourView(true); hour=prefs.getInt("sleep_hour",22); minute=prefs.getInt("sleep_minute",0) }; c.addView(st,LinearLayout.LayoutParams(-1,dp(150))); val alarm=CheckBox(this).apply { text="Огоҳиномаи садодор"; isChecked=prefs.getBoolean("alarm_enabled",true); textSize=15f }; c.addView(alarm); content.addView(c); space(12)
        content.addView(button("💾  Захира ва фаъол кардани ёдраскуниҳо") {
            prefs.edit().putBoolean("workout_enabled",work.isChecked).putInt("workout_hour",wt.hour).putInt("workout_minute",wt.minute).putBoolean("sleep_enabled",sleep.isChecked).putInt("sleep_hour",st.hour).putInt("sleep_minute",st.minute).putBoolean("notification_enabled",true).putBoolean("alarm_enabled",alarm.isChecked).apply()
            ReminderScheduler.cancelAll(this); val selected=prefs.getStringSet("days",setOf("0","2","4"))?:setOf("0","2","4")
            if(work.isChecked) selected.forEach { d->ReminderScheduler.scheduleWeekly(this,"workout_$d",wt.hour,wt.minute,dayCalendar[d.toInt()],"Вақти машқ расид 💪","Қувват: вақти машқ аст. Оғоз кун!") }
            if(sleep.isChecked) ReminderScheduler.scheduleDaily(this,"sleep",st.hour,st.minute,"Вақти хоб расид 🌙","Барои барқароршавӣ вақти хоб аст.")
            Toast.makeText(this,"Танзимот захира шуд",Toast.LENGTH_LONG).show(); requestPermissionFlow()
        },LinearLayout.LayoutParams(-1,dp(52))); space(8)
        content.addView(button("🔔  Санҷиши огоҳӣ баъди 10 сония") { ReminderScheduler.scheduleOnce(this,"test",System.currentTimeMillis()+10000L,"Санҷиши Қувват 🔔","Агар ин огоҳӣ омад, ёдраскуниҳо фаъоланд."); Toast.makeText(this,"10 сония интизор шав",Toast.LENGTH_SHORT).show() },LinearLayout.LayoutParams(-1,dp(48))); space(8)
        content.addView(button("⚙️  Иҷозатҳои телефон") { requestPermissionFlow(); startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))) },LinearLayout.LayoutParams(-1,dp(48))); space(12)
        val info=card(); info.addView(text("Ҳолати иҷозатҳо",16f,INK,true)); info.addView(text(permissionStatus(),13f,MUTED)); info.addView(text("Барои огоҳӣ ҳангоми баста будани барнома, дар баъзе телефонҳо иҷозати огоҳинома, ҳушдори дақиқ ва кори пасзамина лозим аст. Танзимоти батареяи телефон низ метавонад таъсир расонад.",13f,MUTED)); content.addView(info)
    }
    private fun requestPermissionFlow() {
        if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS),100)
        if(Build.VERSION.SDK_INT>=31 && !getSystemService(AlarmManager::class.java).canScheduleExactAlarms()) startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,Uri.parse("package:$packageName")))
    }
    private fun permissionStatus():String {
        val n=if(Build.VERSION.SDK_INT<33||checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED) "Огоҳинома: фаъол" else "Огоҳинома: иҷозат лозим"
        val a=if(Build.VERSION.SDK_INT<31||getSystemService(AlarmManager::class.java).canScheduleExactAlarms()) "Ҳушдори дақиқ: иҷозат ҳаст" else "Ҳушдори дақиқ: иҷозат лозим"
        return "$n\n$a"
    }
}

object ReminderScheduler {
    fun scheduleWeekly(context:Context,key:String,hour:Int,minute:Int,dayOfWeek:Int,title:String,message:String) {
        val next=Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY,hour);set(Calendar.MINUTE,minute);set(Calendar.SECOND,0);set(Calendar.MILLISECOND,0);while(get(Calendar.DAY_OF_WEEK)!=dayOfWeek||timeInMillis<=System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR,1) }
        scheduleRepeating(context,key,next.timeInMillis,7*24*60*60*1000L,title,message)
    }
    fun scheduleDaily(context:Context,key:String,hour:Int,minute:Int,title:String,message:String) {
        val next=Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY,hour);set(Calendar.MINUTE,minute);set(Calendar.SECOND,0);set(Calendar.MILLISECOND,0);if(timeInMillis<=System.currentTimeMillis())add(Calendar.DAY_OF_YEAR,1) }
        scheduleRepeating(context,key,next.timeInMillis,24*60*60*1000L,title,message)
    }
    fun scheduleOnce(context:Context,key:String,time:Long,title:String,message:String) {
        val intent=Intent(context,ReminderReceiver::class.java).apply{putExtra("key",key);putExtra("title",title);putExtra("message",message)}
        val pi=PendingIntent.getBroadcast(context,key.hashCode(),intent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE); val am=context.getSystemService(AlarmManager::class.java)
        try{am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,time,pi)}catch(_:SecurityException){am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,time,pi)}
    }
    private fun scheduleRepeating(context:Context,key:String,time:Long,interval:Long,title:String,message:String) {
        val intent=Intent(context,ReminderReceiver::class.java).apply{putExtra("key",key);putExtra("title",title);putExtra("message",message);putExtra("interval",interval)}
        val pi=PendingIntent.getBroadcast(context,key.hashCode(),intent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE);val am=context.getSystemService(AlarmManager::class.java)
        try{am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,time,pi)}catch(_:SecurityException){am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,time,pi)}
    }
    fun cancelAll(context:Context) { val am=context.getSystemService(AlarmManager::class.java);(0..6).map{"workout_$it"}.plus(listOf("sleep","test")).forEach{key->val pi=PendingIntent.getBroadcast(context,key.hashCode(),Intent(context,ReminderReceiver::class.java),PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE);if(pi!=null)am.cancel(pi)} }
}
