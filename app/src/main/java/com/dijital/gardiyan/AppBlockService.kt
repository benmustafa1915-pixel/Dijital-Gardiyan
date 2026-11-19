package com.dijital.gardiyan

import android.accessibilityservice.AccessibilityService
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.TextView

class AppBlockService : AccessibilityService() {

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private var strikeCount = 0
    private lateinit var dpm: DevicePolicyManager
    private lateinit var adminComponent: ComponentName
    
    // Kendini ve sistemin kritik parçalarını engelleme koruması
    private val forbiddenPackages = listOf("com.dijital.gardiyan", "com.android.systemui")
    
    // Engellenen uygulamalar (Senin listene göre güncellenecek)
    private val blockedApps = listOf("com.android.settings", "com.android.vending", "com.instagram.android")

    override fun onServiceConnected() {
        super.onServiceConnected()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminComponent = ComponentName(this, AdminReceiver::class.java)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return

        // Launcher ve kendi uygulamamızı kesinlikle engellemiyoruz
        if (forbiddenPackages.contains(packageName) || isLauncher(packageName)) return

        if (blockedApps.contains(packageName)) {
            val prefs = getSharedPreferences("GardiyanPrefs", Context.MODE_PRIVATE)
            val unlockTime = prefs.getLong("unlockTime", 0)
            
            // Eğer zaman kilidi aktifse (veya normal engel)
            if (System.currentTimeMillis() < unlockTime || unlockTime == 0L) {
                executeGhostMode(packageName)
            }
        }
    }

    private fun executeGhostMode(pkgName: String) {
        // 1. Anında Uygulamayı Öldür (Arka planda)
        val am = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        am.killBackgroundProcesses(pkgName)
        
        // 2. Ekrana Perde İndir (3 Saniyelik Ceza)
        showOverlay()
        
        // 3. İhlal Cezası (3 Strike)
        strikeCount++
        if (strikeCount >= 3) {
            strikeCount = 0
            if (dpm.isAdminActive(adminComponent)) {
                dpm.lockNow() // Telefonu anında kilitle
            }
        }
        
        // 4. Geri Tuşuna Bas ve Ana Ekrana Dön
        performGlobalAction(GLOBAL_ACTION_BACK)
        Handler(Looper.getMainLooper()).postDelayed({
            performGlobalAction(GLOBAL_ACTION_HOME)
        }, 100)
    }

    private fun showOverlay() {
        if (overlayView != null) return
        
        overlayView = View(this).apply {
            setBackgroundColor(android.graphics.Color.parseColor("#E6000000")) // Siyah transparan
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        )

        windowManager.addView(overlayView, params)

        // 3 Saniye Geri Sayım ve Ekranı Kapat
        object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Sade tutuyoruz
            }
            override fun onFinish() {
                if (overlayView != null) {
                    windowManager.removeView(overlayView)
                    overlayView = null
                }
            }
        }.start()
    }

    private fun isLauncher(packageName: String): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val res = packageManager.resolveActivity(intent, 0)
        return res?.activityInfo?.packageName == packageName
    }

    override fun onInterrupt() {}
}
