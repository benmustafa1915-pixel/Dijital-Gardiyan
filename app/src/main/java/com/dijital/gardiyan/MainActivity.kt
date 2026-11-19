package com.dijital.gardiyan

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var dpm: DevicePolicyManager
    private lateinit var adminComponent: ComponentName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminComponent = ComponentName(this, AdminReceiver::class.java)

        // Pil Optimizasyon İzni İsteme (Canavar gibi çalışması için)
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }

        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnToggleStrict = findViewById<Button>(R.id.btnToggleStrict)
        val btnEnableAccessibility = findViewById<Button>(R.id.btnEnableAccessibility)
        val btnHideIcon = findViewById<Button>(R.id.btnHideIcon)

        // Cihaz Yöneticisi Aktif mi?
        if (!dpm.isAdminActive(adminComponent)) {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
            startActivity(intent)
        }

        btnEnableAccessibility.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            Toast.makeText(this, "Gardiyan Servisini Açın!", Toast.LENGTH_LONG).show()
        }

        btnToggleStrict.setOnClickListener {
            val pass = etPassword.text.toString()
            if (pass.isEmpty() || pass.length < 4) {
                Toast.makeText(this, "En az 4 haneli şifre girin!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Basit bir mantıkla süreyi 1 saat sonrasına ayarlıyoruz (Geliştirilebilir)
            val unlockTime = System.currentTimeMillis() + 3600000 // 1 Saat
            
            val prefs = getSharedPreferences("GardiyanPrefs", Context.MODE_PRIVATE)
            prefs.edit().putLong("unlockTime", unlockTime).putString("masterPass", pass).apply()
            
            Toast.makeText(this, "Sıkı Mod Aktif! 1 saat boyunca kilitler açılmaz.", Toast.LENGTH_LONG).show()
            finishAffinity()
        }

        // Simgeyi Gizleme
        btnHideIcon.setOnClickListener {
            val p = packageManager
            val componentName = ComponentName(this, MainActivity::class.java)
            
            // Simgeyi gizle
            p.setComponentEnabledSetting(
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
            Toast.makeText(this, "Simge gizlendi! Arama ekranından *#*#1234#*#* ile açabilirsiniz.", Toast.LENGTH_LONG).show()
        }
    }
}
