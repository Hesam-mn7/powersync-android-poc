package com.example.powersync.domain.repository

import com.example.powersync.domain.model.Customer
import kotlinx.coroutines.flow.Flow

/**
 * Created by H.Mousavioun on 12/2/2025
 */
interface CustomerRepository {
    fun getCustomers(): Flow<List<Customer>>
    suspend fun addCustomer(customer: Customer)
    suspend fun updateCustomer(customer: Customer)
    suspend fun deleteCustomer(customer: Customer)
    suspend fun deleteAll()
}