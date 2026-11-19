package com.dijital.gardiyan

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AppsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_apps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewApps)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Telefondaki yüklü uygulamaları çek
        val pm = requireActivity().packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        
        // Kendi uygulamamızı listeden çıkaralım
        val filteredApps = apps.filter { it.packageName != requireActivity().packageName && pm.getLaunchIntentForPackage(it.packageName) != null }

        recyclerView.adapter = AppAdapter(filteredApps, pm)
    }

    // Uygulamaları listeleyecek adaptör
    class AppAdapter(private val apps: List<ApplicationInfo>, private val pm: PackageManager) : RecyclerView.Adapter<AppAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvAppName: TextView = view.findViewById(android.R.id.text1)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            // Android'in standart basit liste tasarımını kullanıyoruz
            val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val app = apps[position]
            holder.tvAppName.text = app.loadLabel(pm).toString()
            holder.tvAppName.setTextColor(android.graphics.Color.WHITE)
            holder.tvAppName.textSize = 18f
            holder.itemView.setPadding(0, 30, 0, 30)
        }

        override fun getItemCount() = apps.size
    }
}
