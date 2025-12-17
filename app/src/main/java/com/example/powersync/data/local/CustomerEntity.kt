package com.example.powersync.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by H.Mousavioun on 12/2/2025
 */
@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey
    val id: String,
    val customerName: String,
    val description: String
)