package com.example.powersync.data.repository

import com.example.powersync.data.local.CustomerDao
import com.example.powersync.data.local.CustomerEntity
import com.example.powersync.data.sync.PowerSyncClientHolder
import com.example.powersync.domain.model.Customer
import com.example.powersync.domain.repository.CustomerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Created by H.Mousavioun on 12/2/2025
 */
class CustomerRepositoryImpl(
    private val dao: CustomerDao
) : CustomerRepository {

    override fun getCustomers(): Flow<List<Customer>> {
        return dao.getCustomers().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun addCustomer(customer: Customer) {
        dao.insertCustomer(customer.toEntity())
        PowerSyncClientHolder.pool.transferPendingRoomUpdatesToPowerSync()
    }

    override suspend fun updateCustomer(customer: Customer) {
        dao.updateCustomer(customer.toEntity())
        PowerSyncClientHolder.pool.transferPendingRoomUpdatesToPowerSync()
    }

    override suspend fun deleteCustomer(customer: Customer) {
        dao.deleteCustomer(customer.toEntity())
        PowerSyncClientHolder.pool.transferPendingRoomUpdatesToPowerSync()
    }

    override suspend fun deleteAll() {
        dao.deleteAll()
        PowerSyncClientHolder.pool.transferPendingRoomUpdatesToPowerSync()
    }

    // Mapping helpers
    private fun CustomerEntity.toDomain(): Customer =
        Customer(id = id, customername = customername, description = description)

    private fun Customer.toEntity(): CustomerEntity =
        CustomerEntity(
            id = id,
            customername = customername,
            description = description
        )

}
