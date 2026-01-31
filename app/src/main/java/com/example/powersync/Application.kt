package com.example.powersync

import android.app.Application
import android.util.Log
import com.example.powersync.data.local.AppDatabase
import com.example.powersync.data.sync.DemoConnector
import com.example.powersync.data.sync.PowerSyncClientHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Created by H.Mousavioun on 12/19/2025
 */

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        Log.e("PSYNC", "App.onCreate() called")

        val roomDb = AppDatabase.getInstance(this)

        Log.e("PSYNC", "PowerSync init() in Application")
        PowerSyncClientHolder.init(context = this, roomDb = roomDb)

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            PowerSyncClientHolder.installCrudTriggers()
            val hostIp = "192.168.1.35" //API
            Log.e("PSYNC", "Connecting to PowerSync host=$hostIp")
            PowerSyncClientHolder.connect(
                DemoConnector(hostIp = hostIp)
            )
            Log.e("PSYNC", "PowerSync connect() DONE")
        }
    }
}

