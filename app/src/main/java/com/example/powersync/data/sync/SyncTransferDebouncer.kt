package com.example.powersync.data.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created by H.Mousavioun on 12/23/2025
 */
object SyncTransferDebouncer {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var job: Job? = null

    fun requestTransfer(delayMs: Long = 250L) {
        job?.cancel()
        job = scope.launch {
            delay(delayMs)
            PowerSyncClientHolder.pool.transferPendingRoomUpdatesToPowerSync()
        }
    }

    suspend fun flushNow() {
        job?.cancel()
        job = null
        PowerSyncClientHolder.pool.transferPendingRoomUpdatesToPowerSync()
    }
}