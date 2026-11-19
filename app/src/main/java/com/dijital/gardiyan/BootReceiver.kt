package com.dijital.gardiyan

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Telefon açıldığında erişilebilirlik servisi Android tarafından otomatik başlatılır.
            // Gelecekte buraya ekstra arka plan servisleri eklenebilir.
        }
    }
}
