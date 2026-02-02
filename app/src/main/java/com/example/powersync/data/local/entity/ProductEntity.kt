package com.example.powersync.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by H.Mousavioun on 2/1/2026
 */
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val productname: String,
    val productcode: String
)