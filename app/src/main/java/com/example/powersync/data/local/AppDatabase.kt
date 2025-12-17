package com.example.powersync.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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

                val driver = BundledSQLiteDriver().also {
                    it.loadPowerSyncExtension() // ✅ از PowerSync
                }

                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_db"
                )
                    .setDriver(driver) // ✅ مهم
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}