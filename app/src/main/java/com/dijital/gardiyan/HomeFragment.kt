package com.dijital.gardiyan

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etHomePassword = view.findViewById<EditText>(R.id.etHomePassword)
        val btnStartStrict = view.findViewById<Button>(R.id.btnStartStrict)
        val tvStrictStatus = view.findViewById<TextView>(R.id.tvStrictStatus)

        val prefs = requireActivity().getSharedPreferences("GardiyanPrefs", Context.MODE_PRIVATE)
        val savedMasterPass = prefs.getString("masterPass", null)
        
        // Eğer şifre henüz belirlenmemişse
        if (savedMasterPass == null) {
            btnStartStrict.text = "ŞİFREYİ BELİRLE VE KAYDET"
            btnStartStrict.setOnClickListener {
                val pass = etHomePassword.text.toString()
                if (pass.length < 4) {
                    Toast.makeText(requireContext(), "En az 4 haneli şifre girin!", Toast.LENGTH_SHORT).show()
                } else {
                    prefs.edit().putString("masterPass", pass).apply()
                    Toast.makeText(requireContext(), "Şifre Kaydedildi!", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // Zamanlayıcıyı kontrol et
            val unlockTime = prefs.getLong("unlockTime", 0)
            if (System.currentTimeMillis() < unlockTime) {
                tvStrictStatus.text = "Durum: SIKI MOD AKTİF!"
                tvStrictStatus.setTextColor(android.graphics.Color.parseColor("#FF5722"))
            }

            btnStartStrict.setOnClickListener {
                val pass = etHomePassword.text.toString()
                if (pass == savedMasterPass) {
                    val newUnlockTime = System.currentTimeMillis() + 3600000 // 1 saat
                    prefs.edit().putLong("unlockTime", newUnlockTime).apply()
                    Toast.makeText(requireContext(), "Sıkı Mod Aktif! 1 Saat.", Toast.LENGTH_LONG).show()
                    tvStrictStatus.text = "Durum: SIKI MOD AKTİF!"
                    tvStrictStatus.setTextColor(android.graphics.Color.parseColor("#FF5722"))
                } else {
                    Toast.makeText(requireContext(), "Hatalı Şifre!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
