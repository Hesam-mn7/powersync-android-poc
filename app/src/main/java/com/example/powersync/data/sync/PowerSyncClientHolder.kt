package com.example.powersync.data.sync

import android.content.Context
import co.touchlab.kermit.Logger
import com.example.powersync.data.local.AppDatabase
import com.powersync.ExperimentalPowerSyncAPI
import com.powersync.PowerSyncDatabase
import com.powersync.connectors.PowerSyncBackendConnector
import com.powersync.integrations.room.RoomConnectionPool
import com.powersync.sync.SyncOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Created by H.Mousavioun on 12/16/2025
 */
object PowerSyncClientHolder {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    lateinit var pool: RoomConnectionPool
        private set

    lateinit var psdb: PowerSyncDatabase
        private set

    @OptIn(ExperimentalPowerSyncAPI::class)
    fun init(context: Context, roomDb: AppDatabase) {
        pool = RoomConnectionPool(roomDb, powerSyncSchema)

        val psLogger = Logger.withTag("PowerSync")
        psdb = PowerSyncDatabase.opened(
            pool = pool,
            scope = scope,
            schema = powerSyncSchema,
            identifier = "app_db", // بهتره همون نام DB باشه
            logger = psLogger
        )
    }

    @OptIn(ExperimentalPowerSyncAPI::class)
    suspend fun connect(connector: PowerSyncBackendConnector) {
        psdb.connect(
            connector,
            options = SyncOptions(
                newClientImplementation = true // ✅ RawTable requirement
            )
        )
    }
}