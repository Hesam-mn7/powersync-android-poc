package com.example.powersync.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by H.Mousavioun on 12/2/2025
 */
@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "customername")
    val customername: String,
    val description: String
)