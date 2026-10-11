package ir.nexa.mobile

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NexaWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { updateOne(context, manager, it) }
    }

    companion object {
        fun refresh(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, NexaWidgetProvider::class.java)
            manager.getAppWidgetIds(component).forEach { updateOne(context, manager, it) }
        }

        private fun updateOne(context: Context, manager: AppWidgetManager, id: Int) {
            val views = RemoteViews(context.packageName, R.layout.nexa_widget)
            val prefs = context.getSharedPreferences("nexa_native", Context.MODE_PRIVATE)
            val timerEnd = prefs.getLong("timer_end", 0L)
            val running = prefs.getBoolean("timer_running", false) && timerEnd > System.currentTimeMillis()
            if (running) {
                val mins = ((timerEnd - System.currentTimeMillis() + 59_999) / 60_000).toInt()
                views.setTextViewText(R.id.widget_title, "تایمر فعال")
                views.setTextViewText(R.id.widget_detail, prefs.getString("timer_label", "تمرکز") ?: "تمرکز")
                views.setTextViewText(R.id.widget_time, String.format(Locale.getDefault(), "%02d:%02d", mins, ((timerEnd - System.currentTimeMillis()) / 1000 % 60)))
            } else {
                var taskTitle = "برای امروز برنامه‌ای نداری"
                var taskTime = "Nexa را باز کن"
                try {
                    val root = JSONObject(prefs.getString("state", "{}") ?: "{}")
                    val arr = root.optJSONArray("tasks")
                    val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                    var bestTime = "99:99"
                    if (arr != null) {
                        for (i in 0 until arr.length()) {
                            val t = arr.optJSONObject(i) ?: continue
                            if (t.optString("scheduledDate") != date || t.optString("status") == "completed") continue
                            val time = t.optString("startTime", "99:99")
                            if (time < bestTime) { bestTime = time; taskTitle = t.optString("title", "کار امروز"); taskTime = if (time == "99:99") "بدون ساعت" else time }
                        }
                    }
                } catch (_: Exception) {}
                views.setTextViewText(R.id.widget_title, "Nexa · امروز")
                views.setTextViewText(R.id.widget_detail, taskTitle)
                views.setTextViewText(R.id.widget_time, taskTime)
            }
            val launch = PendingIntent.getActivity(
                context, 801, Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, launch)
            manager.updateAppWidget(id, views)
        }
    }
}
