package com.example.powersync.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.powersync.integrations.room.loadPowerSyncExtension

/**
 * Created by H.Mousavioun on 12/2/2025
 */
@Database(
    entities = [CustomerEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun customerDao(): CustomerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDb(context).also { INSTANCE = it }
            }
        }

        private fun buildDb(context: Context): AppDatabase {
            val driver = BundledSQLiteDriver().also {
                it.loadPowerSyncExtension()
            }

            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "app_db"
            )
                .setDriver(driver)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)

                        db.execSQL("DROP TRIGGER IF EXISTS customers_insert;")
                        db.execSQL("DROP TRIGGER IF EXISTS customers_update;")
                        db.execSQL("DROP TRIGGER IF EXISTS customers_delete;")

                        //INSERT => PUT
                        db.execSQL(
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

                        //UPDATE => PATCH
                        db.execSQL(
                            """
                            CREATE TRIGGER customers_update
                            AFTER UPDATE ON customers
                            FOR EACH ROW
                            BEGIN
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

                        //DELETE => DELETE
                        db.execSQL(
                            """
                            CREATE TRIGGER customers_delete
                            AFTER DELETE ON customers
                            FOR EACH ROW
                            BEGIN
                              INSERT INTO powersync_crud (op, id, type)
                              VALUES (
                                'DELETE',
                                OLD.id,
                                'customers'
                              );
                            END;
                            """.trimIndent()
                        )
                    }
                })
                .build()
        }
    }
}