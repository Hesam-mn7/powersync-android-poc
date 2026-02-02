package com.example.powersync.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import com.example.powersync.data.local.dao.CustomerDao
import com.example.powersync.data.local.dao.ProductDao
import com.example.powersync.data.local.entity.CustomerEntity
import com.example.powersync.data.local.entity.ProductEntity
import com.powersync.integrations.room.loadPowerSyncExtension

/**
 * Created by H.Mousavioun on 12/2/2025
 */
@Database(
    entities = [CustomerEntity::class, ProductEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun customerDao(): CustomerDao
    abstract fun productDao(): ProductDao

    companion object {
        const val DB_NAME = "app_db"
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDb(context).also { INSTANCE = it }
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE customers ADD COLUMN customercode TEXT NOT NULL DEFAULT ''")
            }

            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE customers ADD COLUMN customercode TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS products (
                      id TEXT NOT NULL PRIMARY KEY,
                      productname TEXT NOT NULL,
                      productcode TEXT NOT NULL
                    )
                    """.trimIndent()
                )
            }

            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS products (
                      id TEXT NOT NULL PRIMARY KEY,
                      productname TEXT NOT NULL,
                      productcode TEXT NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        private fun buildDb(context: Context): AppDatabase {
            val driver = BundledSQLiteDriver().also {
                it.loadPowerSyncExtension()
            }

            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DB_NAME
            )
                .setDriver(driver)
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
        }
    }
}