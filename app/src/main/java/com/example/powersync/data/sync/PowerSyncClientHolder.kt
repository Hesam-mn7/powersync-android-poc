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
            identifier = "app_db",
            logger = psLogger
        )
    }

    @OptIn(ExperimentalPowerSyncAPI::class)
    suspend fun installCrudTriggers() {
        psdb.writeTransaction { tx ->

            tx.execute("DROP TRIGGER IF EXISTS customers_insert;")
            tx.execute("DROP TRIGGER IF EXISTS customers_update;")
            tx.execute("DROP TRIGGER IF EXISTS customers_delete;")

            // INSERT => PUT
            tx.execute(
                """
            CREATE TRIGGER customers_insert
            AFTER INSERT ON customers
            FOR EACH ROW
            BEGIN
              INSERT INTO powersync_crud (op, id, type, data)
              VALUES (
                'PUT',
                NEW.id,
                'customers',
                json_object(
                  'id', NEW.id,
                  'customername', NEW.customername,
                  'description', NEW.description
                )
              );
            END;
            """.trimIndent()
            )

            // UPDATE => PATCH
            tx.execute(
                """
            CREATE TRIGGER customers_update
            AFTER UPDATE ON customers
            FOR EACH ROW
            BEGIN
              SELECT CASE
                WHEN (OLD.id != NEW.id)
                THEN RAISE (FAIL, 'Cannot update id')
              END;

              INSERT INTO powersync_crud (op, id, type, data)
              VALUES (
                'PATCH',
                NEW.id,
                'customers',
                json_object(
                  'id', NEW.id,
                  'customername', NEW.customername,
                  'description', NEW.description
                )
              );
            END;
            """.trimIndent()
            )

            // DELETE => DELETE
            tx.execute(
                """
            CREATE TRIGGER customers_delete
            AFTER DELETE ON customers
            FOR EACH ROW
            BEGIN
              INSERT INTO powersync_crud (op, id, type)
              VALUES ('DELETE', OLD.id, 'customers');
            END;
            """.trimIndent()
            )
        }
    }


    @OptIn(ExperimentalPowerSyncAPI::class)
    suspend fun connect(connector: PowerSyncBackendConnector) {
        psdb.connect(
            connector,
            options = SyncOptions(
                newClientImplementation = true
            )
        )
    }
}