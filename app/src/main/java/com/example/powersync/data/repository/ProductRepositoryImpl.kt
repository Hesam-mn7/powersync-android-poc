package com.example.powersync.data.repository

import com.example.powersync.data.local.dao.ProductDao
import com.example.powersync.data.local.entity.ProductEntity
import com.example.powersync.domain.model.Product
import com.example.powersync.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Created by H.Mousavioun on 2/1/2026
 */

class ProductRepositoryImpl(
    private val dao: ProductDao
) : ProductRepository {

    override fun getProducts(): Flow<List<Product>> =
        dao.getProducts().map { list -> list.map { it.toDomain() } }

    override suspend fun addProduct(product: Product) {
        dao.insertProduct(product.toEntity())
    }

    override suspend fun addProducts(items: List<Product>) {
        dao.insertProducts(items.map { it.toEntity() })
    }

    override suspend fun updateProduct(product: Product) {
        dao.updateProduct(product.toEntity())
    }

    override suspend fun deleteProduct(product: Product) {
        dao.deleteProduct(product.toEntity())
    }

    override suspend fun deleteAll() {
        dao.deleteAll()
    }

    override suspend fun getById(id: String): Product? =
        dao.getById(id)?.toDomain()

    override suspend fun getByIds(ids: List<String>): List<Product> =
        dao.getByIds(ids).map { it.toDomain() }

    private fun ProductEntity.toDomain(): Product =
        Product(
            id = id,
            productname = productname,
            productcode = productcode
        )

    private fun Product.toEntity(): ProductEntity =
        ProductEntity(
            id = id,
            productname = productname,
            productcode = productcode
        )
}
