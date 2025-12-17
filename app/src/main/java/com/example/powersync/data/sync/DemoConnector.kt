package com.example.powersync.data.sync

import com.powersync.PowerSyncDatabase
import com.powersync.connectors.PowerSyncBackendConnector
import com.powersync.connectors.PowerSyncCredentials

/**
 * Created by H.Mousavioun on 12/17/2025
 */
class DemoConnector : PowerSyncBackendConnector() {
    override suspend fun fetchCredentials(): PowerSyncCredentials {
        // فعلاً بعداً با سرور واقعی پرش می‌کنیم
        // باید JWT + powersync service url بده
        TODO("Implement after server is ready")
    }

    override suspend fun uploadData(database: PowerSyncDatabase) {
        // وقتی سرور آماده شد اینجا transaction CRUD رو می‌گیریم و می‌فرستیم به backend
        // نمونه‌ی getNextCrudTransaction در docs هست
        val tx = database.getNextCrudTransaction() ?: return
        TODO("Send tx.crud to backend then tx.complete(null)")
    }
}