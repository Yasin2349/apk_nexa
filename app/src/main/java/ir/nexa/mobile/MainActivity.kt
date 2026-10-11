package ir.nexa.mobile

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.graphics.Color
import android.view.View
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.util.Locale

class MainActivity : Activity() {
    private lateinit var web: WebView
    private val appHost = "appassets.androidplatform.net"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(11, 17, 32)
        window.navigationBarColor = Color.rgb(11, 17, 32)
        window.decorView.systemUiVisibility = 0
        createChannels()
        askPermissionsIfNeeded()

        web = WebView(this)
        web.setBackgroundColor(Color.rgb(11, 17, 32))
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.databaseEnabled = true
        web.settings.allowFileAccess = false
        web.settings.allowContentAccess = false
        web.settings.javaScriptCanOpenWindowsAutomatically = false
        web.settings.textZoom = 100
        web.settings.loadsImagesAutomatically = true
        web.settings.setSupportMultipleWindows(false)
        web.addJavascriptInterface(NexaBridge(this), "NexaNative")
        web.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                val uri = request?.url ?: return null
                if (uri.host != appHost) return null
                var path = Uri.decode(uri.path ?: "/").trimStart('/')
                if (path.isBlank()) path = "index.html"
                if (path.contains("..")) return response("Not found", "text/plain", 404)
                val assets = this@MainActivity.assets
                return try {
                    val stream = assets.open("www/$path")
                    response(stream, mime(path), 200)
                } catch (_: Exception) {
                    // Nuxt client-side routes should all resolve to the same app shell.
                    try {
                        response(assets.open("www/index.html"), "text/html", 200)
                    } catch (_: Exception) {
                        response("Nexa assets missing", "text/plain", 404)
                    }
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                injectNativeHooks()
                // Push current persisted state to Android immediately after page load.
                web.evaluateJavascript(
                    "(function(){try{var s=localStorage.getItem('nexa-productivity-v2');if(s&&window.NexaNative)NexaNative.onStateChanged(s)}catch(e){}})();",
                    null
                )
            }
        }
        setContentView(web)
        web.loadUrl("https://$appHost/")
    }

    private fun response(text: String, mime: String, status: Int): WebResourceResponse =
        response(ByteArrayInputStream(text.toByteArray()), mime, status)

    private fun response(stream: java.io.InputStream, mime: String, status: Int): WebResourceResponse {
        val reason = if (status == 200) "OK" else "Not Found"
        return WebResourceResponse(mime, "UTF-8", status, reason, mapOf("Cache-Control" to "no-cache"), stream)
    }

    private fun mime(path: String): String = when {
        path.endsWith(".html") -> "text/html"
        path.endsWith(".js") || path.endsWith(".mjs") -> "application/javascript"
        path.endsWith(".css") -> "text/css"
        path.endsWith(".json") || path.endsWith(".webmanifest") -> "application/json"
        path.endsWith(".svg") -> "image/svg+xml"
        path.endsWith(".png") -> "image/png"
        path.endsWith(".jpg") || path.endsWith(".jpeg") -> "image/jpeg"
        path.endsWith(".webp") -> "image/webp"
        path.endsWith(".ico") -> "image/x-icon"
        path.endsWith(".woff2") -> "font/woff2"
        path.endsWith(".woff") -> "font/woff"
        path.endsWith(".ttf") -> "font/ttf"
        else -> "application/octet-stream"
    }

    private fun injectNativeHooks() {
        val js = """
          (function(){
            try {
              if (!window.__nexaNativeHooked) {
                window.__nexaNativeHooked = true;
                var originalSet = Storage.prototype.setItem;
                Storage.prototype.setItem = function(k, v) {
                  originalSet.call(this, k, v);
                  if (k === 'nexa-productivity-v2' && window.NexaNative) {
                    try { window.NexaNative.onStateChanged(String(v)); } catch(e) {}
                  }
                };
              }
              if (!document.getElementById('nexa-mobile-nav-style')) {
                var style = document.createElement('style');
                style.id = 'nexa-mobile-nav-style';
                style.textContent = `
                  @media(max-width:760px){
                    body{padding-bottom:82px!important}
                    #nexa-mobile-bottom{position:fixed;z-index:99999;bottom:0;left:0;right:0;display:grid;grid-template-columns:repeat(5,1fr);padding:7px 5px calc(7px + env(safe-area-inset-bottom));background:rgba(10,16,30,.97);border-top:1px solid rgba(148,163,184,.22);backdrop-filter:blur(18px);font-family:inherit}
                    #nexa-mobile-bottom a{display:flex;flex-direction:column;align-items:center;justify-content:center;gap:3px;color:#aab7cb;text-decoration:none;font-size:10px;font-weight:700;min-height:48px}
                    #nexa-mobile-bottom a span:first-child{font-size:19px;line-height:22px}
                    #nexa-mobile-bottom a.active{color:#6ee7b7}
                    header{padding-top:env(safe-area-inset-top)}
                  }
                  /* Nexa Android: touch-first, readable mobile styling */
                  :root{color-scheme:dark!important}
                  html,body,#__nuxt{width:100%;min-height:100%;margin:0!important;background:#0b1120!important;color:#e5e7eb!important;direction:rtl!important;overflow-x:hidden!important}
                  body{font-family:system-ui,-apple-system,sans-serif!important;font-size:15px!important;line-height:1.75!important;padding-bottom:calc(88px + env(safe-area-inset-bottom))!important;-webkit-text-size-adjust:100%}
                  *,*:before,*:after{box-sizing:border-box}
                  a{color:#a5b4fc;text-decoration:none!important} a:visited{color:#c4b5fd}
                  button,[role=button],input,select,textarea{font:inherit!important;max-width:100%;touch-action:manipulation}
                  input,select,textarea{min-height:44px!important;color:#f1f5f9!important;background:#111c30!important;border:1px solid #334155!important;border-radius:12px!important;padding:9px 12px!important}
                  button,[role=button]{min-height:42px}
                  main{min-width:0!important;width:100%!important;padding:14px!important;margin:0!important}
                  main>div,main section{min-width:0;max-width:100%}
                  [data-slot=root]{max-width:100%;border-radius:18px!important;border-color:rgba(148,163,184,.18)!important;background:#111b2d!important;box-shadow:0 8px 24px rgba(0,0,0,.12)}
                  [data-slot=header]{padding:14px 16px!important;color:#f8fafc!important}
                  [data-slot=body]{padding:14px!important;color:#dbe4f0!important}
                  h1{font-size:clamp(24px,6vw,32px)!important;line-height:1.4!important;color:#f8fafc!important;margin-block:12px 18px!important}
                  h2{font-size:21px!important;line-height:1.45!important;color:#f8fafc!important} h3{font-size:17px!important;color:#f8fafc!important}
                  p{line-height:1.9!important}
                  .text-gray-500,.text-gray-600{color:#aab8cc!important}
                  .bg-white,.bg-default{background:#111b2d!important}
                  .text-black,.text-gray-900,.text-gray-950{color:#e5e7eb!important}
                  .text-primary{color:#a5b4fc!important}
                  .underline{text-decoration:none!important}
                  #nexa-mobile-bottom{padding-bottom:calc(8px + env(safe-area-inset-bottom))!important}
                  #nexa-mobile-bottom a{color:#94a3b8!important;text-decoration:none!important;font-size:11px!important;min-height:52px!important}
                  #nexa-mobile-bottom a.active{color:#6ee7b7!important}
                  #nexa-mobile-bottom a span:first-child{font-size:21px!important}
                  @media(max-width:420px){main{padding:10px!important}[data-slot=body]{padding:12px!important}.grid{grid-template-columns:minmax(0,1fr)!important}.flex.items-center.gap-4{gap:10px!important;flex-wrap:wrap}h1{font-size:25px!important}}
                  @media(min-width:761px){#nexa-mobile-bottom{display:none!important}}
                `;
                document.head.appendChild(style);
                var nav = document.createElement('nav');
                nav.id = 'nexa-mobile-bottom';
                nav.innerHTML = '<a href="/today"><span>☀</span><span>امروز</span></a><a href="/tasks"><span>✓</span><span>وظایف</span></a><a href="/planner"><span>▦</span><span>برنامه</span></a><a href="/focus"><span>◷</span><span>تمرکز</span></a><a href="/settings"><span>⚙</span><span>تنظیمات</span></a>';
                document.body.appendChild(nav);
                function activeNav(){var p=location.pathname;nav.querySelectorAll('a').forEach(function(a){a.classList.toggle('active',a.getAttribute('href')===p||(p==='/'&&a.getAttribute('href')==='/today'));});}
                activeNav(); window.addEventListener('popstate',activeNav);
                document.addEventListener('click',function(e){var a=e.target.closest&&e.target.closest('#nexa-mobile-bottom a');if(a){setTimeout(activeNav,80);}});
              }
            } catch(e) {}
          })();
        """.trimIndent()
        web.evaluateJavascript(js, null)
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(NotificationChannel(
                TimerNotifications.CHANNEL_TIMER, "تایمر فعال", NotificationManager.IMPORTANCE_LOW
            ).apply { description = "نمایش تایمر در حال اجرا" })
            manager.createNotificationChannel(NotificationChannel(
                TimerNotifications.CHANNEL_ALARM, "پایان تایمر", NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "هشدار پایان تایمر Nexa"
                enableVibration(true)
                setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI, null)
            })
        }
    }

    private fun askPermissionsIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarm = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!alarm.canScheduleExactAlarms()) {
                try {
                    startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName")))
                } catch (_: Exception) {}
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::web.isInitialized) web.postDelayed({
            try {
                web.evaluateJavascript("(function(){try{var s=localStorage.getItem('nexa-productivity-v2');if(s&&window.NexaNative)NexaNative.onStateChanged(s)}catch(e){}})();", null)
            } catch (_: Exception) {}
        }, 250)
    }

    override fun onBackPressed() {
        if (::web.isInitialized && web.canGoBack()) web.goBack() else super.onBackPressed()
    }
}

class NexaBridge(private val context: Context) {
    @JavascriptInterface
    fun onStateChanged(raw: String) {
        try {
            val prefs = context.getSharedPreferences("nexa_native", Context.MODE_PRIVATE)
            prefs.edit().putString("state", raw).apply()
            val root = JSONObject(raw)
            val timer = root.optJSONObject("activeTimer")
            if (timer != null && timer.optString("status") == "running") {
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
            } else {
                val endAt = prefs.getLong("timer_end", 0L)
                val id = prefs.getString("timer_id", "") ?: ""
                val wasRunning = prefs.getBoolean("timer_running", false)
                if (wasRunning && endAt > System.currentTimeMillis() + 1800L) {
                    TimerNotifications.cancel(context)
                    prefs.edit().putBoolean("timer_running", false).putLong("timer_end", 0L).putString("timer_id", "").apply()
                } else if (wasRunning && id.isNotBlank()) {
                    // Keep a near-expiry alarm armed; the JS timer can finish at the same instant.
                    TimerNotifications.keepNearExpiry(context, id)
                } else {
                    TimerNotifications.cancel(context)
                }
            }
            NexaWidgetProvider.refresh(context)
        } catch (_: Exception) {}
    }
}
