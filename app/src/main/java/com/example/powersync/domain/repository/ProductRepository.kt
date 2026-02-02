package com.example.powersync.domain.repository

import com.example.powersync.domain.model.Product
import kotlinx.coroutines.flow.Flow

/**
 * Created by H.Mousavioun on 2/1/2026
 */
interface ProductRepository {
    fun getProducts(): Flow<List<Product>>
    suspend fun addProduct(product: Product)
    suspend fun addProducts(items: List<Product>)
    suspend fun updateProduct(product: Product)
    suspend fun deleteProduct(product: Product)
    suspend fun deleteAll()
    suspend fun getById(id: String): Product?
    suspend fun getByIds(ids: List<String>): List<Product>
}