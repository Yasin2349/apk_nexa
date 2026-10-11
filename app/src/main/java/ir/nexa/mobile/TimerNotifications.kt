package ir.nexa.mobile

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.widget.RemoteViews

object TimerNotifications {
    const val CHANNEL_TIMER = "nexa_timer"
    const val CHANNEL_ALARM = "nexa_timer_finished"
    private const val RUNNING_ID = 7001
    private const val FINISHED_ID = 7002

    fun sync(context: Context, id: String, label: String, endAt: Long) {
        val prefs = context.getSharedPreferences("nexa_native", Context.MODE_PRIVATE)
        val previous = prefs.getString("timer_id", "") ?: ""
        schedule(context, id, endAt)
        val open = PendingIntent.getActivity(
            context, 701, Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, CHANNEL_TIMER)
        } else {
            @Suppress("DEPRECATION") Notification.Builder(context)
        }.setSmallIcon(R.drawable.ic_timer)
            .setContentTitle("Nexa · تایمر فعال")
            .setContentText(label)
            .setWhen(endAt)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(open)
            .build()
        notification.flags = notification.flags or Notification.FLAG_ONGOING_EVENT
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(RUNNING_ID, notification)
    }

    fun keepNearExpiry(context: Context, id: String) {
        val prefs = context.getSharedPreferences("nexa_native", Context.MODE_PRIVATE)
        val endAt = prefs.getLong("timer_end", 0L)
        if (endAt > 0L) schedule(context, id, endAt)
    }

    private fun schedule(context: Context, id: String, endAt: Long) {
        val intent = Intent(context, TimerReceiver::class.java).putExtra("timer_id", id)
        val pi = PendingIntent.getBroadcast(
            context, 702, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarm.canScheduleExactAlarms()) {
                alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endAt, pi)
            } else {
                alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endAt, pi)
            }
        } catch (_: SecurityException) {
            alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endAt, pi)
        }
    }

    fun cancel(context: Context) {
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = PendingIntent.getBroadcast(
            context, 702, Intent(context, TimerReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarm.cancel(pi)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(RUNNING_ID)
    }

    fun finished(context: Context, id: String) {
        val prefs = context.getSharedPreferences("nexa_native", Context.MODE_PRIVATE)
        if ((prefs.getString("timer_id", "") ?: "") != id) return
        val endAt = prefs.getLong("timer_end", 0L)
        if (endAt > System.currentTimeMillis() + 1500L) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(RUNNING_ID)
        val open = PendingIntent.getActivity(
            context, 703, Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val label = prefs.getString("timer_label", "تمرکز") ?: "تمرکز"
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, CHANNEL_ALARM)
        } else {
            @Suppress("DEPRECATION") Notification.Builder(context)
        }
        val notification = builder.setSmallIcon(R.drawable.ic_timer)
            .setContentTitle("تایمر تمام شد")
            .setContentText("زمان «$label» به پایان رسید.")
            .setAutoCancel(true)
            .setContentIntent(open)
            .setDefaults(Notification.DEFAULT_ALL)
            .build()
        manager.notify(FINISHED_ID, notification)
        prefs.edit().putBoolean("timer_running", false).putLong("timer_end", 0L).putString("timer_id", "").apply()
        NexaWidgetProvider.refresh(context)
    }
}

class TimerReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val id = intent?.getStringExtra("timer_id") ?: return
        TimerNotifications.finished(context, id)
    }
}

class BootReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val prefs = context.getSharedPreferences("nexa_native", Context.MODE_PRIVATE)
        val state = prefs.getString("state", null) ?: return
        try {
            val root = org.json.JSONObject(state)
            val timer = root.optJSONObject("activeTimer") ?: return
            if (timer.optString("status") != "running") return
            val id = timer.optString("id")
            val duration = timer.optLong("durationSeconds", 0L)
            val elapsed = timer.optLong("elapsedBeforePause", 0L)
            val startedAt = timer.optLong("startedAt", 0L)
            if (id.isNotBlank() && duration > 0 && startedAt > 0) {
                val endAt = startedAt + (duration - elapsed) * 1000L
                prefs.edit().putString("timer_id", id).putString("timer_label", timer.optString("label", "تمرکز"))
                    .putLong("timer_end", endAt).putBoolean("timer_running", true).apply()
                TimerNotifications.sync(context, id, timer.optString("label", "تمرکز"), endAt)
            }
        } catch (_: Exception) {}
    }
}
