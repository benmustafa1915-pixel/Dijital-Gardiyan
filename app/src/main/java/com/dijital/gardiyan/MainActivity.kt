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

        // Pil Optimizasyon İzni
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

        val prefs = getSharedPreferences("GardiyanPrefs", Context.MODE_PRIVATE)
        var savedMasterPass = prefs.getString("masterPass", null)

        // Cihaz Yöneticisi Aktif mi?
        if (!dpm.isAdminActive(adminComponent)) {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
            startActivity(intent)
        }
        
        // Kaldırma Engellemesini Kontrol Et (Eğer Device Owner isek kaldır butonunu gizle)
        try {
            if (dpm.isDeviceOwnerApp(packageName)) {
                dpm.setUninstallBlocked(adminComponent, packageName, true)
                Toast.makeText(this, "Katı Mod Aktif: Uygulama sistem tarafından kilitlendi!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            // Device Owner değilse hata verir, yoksay.
        }

        btnEnableAccessibility.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            Toast.makeText(this, "Gardiyan Servisini Açın!", Toast.LENGTH_LONG).show()
        }

        // Eğer daha önce şifre belirlenmemişse (İlk Kurulum Modu)
        if (savedMasterPass == null) {
            btnToggleStrict.text = "ŞİFREYİ BELİRLE VE BAŞLA"
            
            btnToggleStrict.setOnClickListener {
                val pass = etPassword.text.toString()
                if (pass.length < 4) {
                    Toast.makeText(this, "En az 4 haneli bir şifre belirleyin!", Toast.LENGTH_SHORT).show()
                } else {
                    prefs.edit().putString("masterPass", pass).apply()
                    Toast.makeText(this, "Yönetici Şifresi Kaydedildi!", Toast.LENGTH_LONG).show()
                    recreate() // Ekranı yenile
                }
            }
            // Şifre belirlenene kadar diğer butonları kilitle
            btnEnableAccessibility.isEnabled = false
            btnHideIcon.isEnabled = false
            
        } else {
            // Şifre zaten belirlenmişse (Normal Kullanım Modu)
            btnToggleStrict.text = "SIKI MODU AÇ (1 SAAT)"
            
            btnToggleStrict.setOnClickListener {
                val pass = etPassword.text.toString()
                if (pass != savedMasterPass) {
                    Toast.makeText(this, "Hatalı Şifre!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                
                // Süreyi 1 saat sonrasına ayarla
                val unlockTime = System.currentTimeMillis() + 3600000
                prefs.edit().putLong("unlockTime", unlockTime).apply()
                
                Toast.makeText(this, "Sıkı Mod Aktif! 1 saat boyunca kilitler açılmaz.", Toast.LENGTH_LONG).show()
                finishAffinity()
            }
        }

        // Simgeyi Gizleme
        btnHideIcon.setOnClickListener {
            val p = packageManager
            val componentName = ComponentName(this, MainActivity::class.java)
            
            p.setComponentEnabledSetting(
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
            Toast.makeText(this, "Simge gizlendi! Arama ekranından *#*#1234#*#* ile açabilirsiniz.", Toast.LENGTH_LONG).show()
        }
    }
}
