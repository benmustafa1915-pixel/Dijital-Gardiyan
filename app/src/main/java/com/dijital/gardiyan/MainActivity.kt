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
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
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

        // Kaldırma Engellemesini Kontrol Et
        try {
            if (dpm.isDeviceOwnerApp(packageName)) {
                dpm.setUninstallBlocked(adminComponent, packageName, true)
            }
        } catch (e: Exception) {
            // Device Owner değilse hata verir, yoksay.
        }

        btnEnableAccessibility.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            Toast.makeText(this, "Gardiyan Servisini Açın!", Toast.LENGTH_LONG).show()
        }

        // --- KLAVYEDEKİ ENTER/BİTTİ TUŞUNU DİNLE ---
        etPassword.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || 
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                
                btnToggleStrict.performClick() // Butona tıklanmış gibi yap
                true
            } else {
                false
            }
        }

        // İlk Kurulum veya Normal Kullanım Mantığı
        if (savedMasterPass == null) {
            btnToggleStrict.text = "ŞİFREYİ BELİRLE VE GİRİŞ YAP"
            
            btnToggleStrict.setOnClickListener {
                val pass = etPassword.text.toString()
                if (pass.length < 4) {
                    Toast.makeText(this, "En az 4 haneli bir şifre belirleyin!", Toast.LENGTH_SHORT).show()
                } else {
                    prefs.edit().putString("masterPass", pass).apply()
                    Toast.makeText(this, "Yönetici Şifresi Kaydedildi! Giriş yapıldı.", Toast.LENGTH_LONG).show()
                    recreate() // Ekranı yenile
                }
            }
            btnEnableAccessibility.isEnabled = false
            btnHideIcon.isEnabled = false
            
        } else {
            btnToggleStrict.text = "SIKI MODU AÇ (1 SAAT)"
            
            btnToggleStrict.setOnClickListener {
                val pass = etPassword.text.toString()
                if (pass != savedMasterPass) {
                    Toast.makeText(this, "Hatalı Şifre! Giriş yapılamadı.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                
                val unlockTime = System.currentTimeMillis() + 3600000
                prefs.edit().putLong("unlockTime", unlockTime).apply()
                
                Toast.makeText(this, "Giriş Başarılı! Sıkı Mod Aktif. 1 saat boyunca kilitler açılmaz.", Toast.LENGTH_LONG).show()
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
