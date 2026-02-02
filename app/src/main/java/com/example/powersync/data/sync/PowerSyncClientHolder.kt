package com.example.powersync.data.sync

import android.content.Context
import co.touchlab.kermit.Logger
import com.example.powersync.data.local.AppDatabase
import com.example.powersync.data.sync.spec.CustomersSpec
import com.example.powersync.data.sync.spec.ProductsSpec
import com.example.powersync.data.sync.spec.TableSpec
import com.powersync.ExperimentalPowerSyncAPI
import com.powersync.PowerSyncDatabase
import com.powersync.connectors.PowerSyncBackendConnector
import com.powersync.db.internal.PowerSyncTransaction
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
            identifier = "app_db",
            logger = psLogger
        )
    }

    @OptIn(ExperimentalPowerSyncAPI::class)
    suspend fun installCrudTriggers() {
        psdb.writeTransaction { tx ->
            installTriggersForSpec(tx, CustomersSpec)
            installTriggersForSpec(tx, ProductsSpec)
        }
    }

    private fun installTriggersForSpec(tx: PowerSyncTransaction, spec: TableSpec<*>) {
        val table = spec.table
        val idCol = spec.idColumn
        val type = spec.type
        val jsonNew = spec.triggerJsonObject("NEW.")

        tx.execute("DROP TRIGGER IF EXISTS ${table}_insert;")
        tx.execute("DROP TRIGGER IF EXISTS ${table}_update;")
        tx.execute("DROP TRIGGER IF EXISTS ${table}_delete;")

        tx.execute(
            """
            CREATE TRIGGER ${table}_insert
            AFTER INSERT ON $table
            FOR EACH ROW
            BEGIN
              INSERT INTO powersync_crud (op, id, type, data)
              VALUES (
                'PUT',
                NEW.$idCol,
                '$type',
                $jsonNew
              );
            END;
            """.trimIndent()
        )

        tx.execute(
            """
            CREATE TRIGGER ${table}_update
            AFTER UPDATE ON $table
            FOR EACH ROW
            BEGIN
              SELECT CASE
                WHEN (OLD.$idCol != NEW.$idCol)
                THEN RAISE (FAIL, 'Cannot update id')
              END;

              INSERT INTO powersync_crud (op, id, type, data)
              VALUES (
                'PATCH',
                NEW.$idCol,
                '$type',
                $jsonNew
              );
            END;
            """.trimIndent()
        )

        tx.execute(
            """
            CREATE TRIGGER ${table}_delete
            AFTER DELETE ON $table
            FOR EACH ROW
            BEGIN
              INSERT INTO powersync_crud (op, id, type)
              VALUES ('DELETE', OLD.$idCol, '$type');
            END;
            """.trimIndent()
        )
    }

    @OptIn(ExperimentalPowerSyncAPI::class)
    suspend fun connect(connector: PowerSyncBackendConnector) {
        psdb.connect(
            connector,
            options = SyncOptions(newClientImplementation = true)
        )
    }
}
