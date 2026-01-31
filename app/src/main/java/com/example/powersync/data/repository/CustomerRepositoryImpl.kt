package com.example.powersync.data.repository

import com.example.powersync.data.local.CustomerDao
import com.example.powersync.data.local.CustomerEntity
import com.example.powersync.data.sync.PowerSyncClientHolder
import com.example.powersync.data.sync.SyncTransferDebouncer
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
        SyncTransferDebouncer.requestTransfer()
    }

    override suspend fun updateCustomer(customer: Customer) {
        dao.updateCustomer(customer.toEntity())
        SyncTransferDebouncer.requestTransfer()
    }

    override suspend fun deleteCustomer(customer: Customer) {
        dao.deleteCustomer(customer.toEntity())
        SyncTransferDebouncer.requestTransfer()
    }

    override suspend fun deleteAll() {
        dao.deleteAll()
        SyncTransferDebouncer.requestTransfer()
    }

    override suspend fun addCustomers(customers: List<Customer>) {
        dao.insertCustomers(customers.map { it.toEntity() })
        SyncTransferDebouncer.flushNow()
    }

    // Mapping helpers
    private fun CustomerEntity.toDomain(): Customer =
        Customer(
            id = id,
            customername = customername,
            description = description,
            customerCode = customerCode
        )

    private fun Customer.toEntity(): CustomerEntity =
        CustomerEntity(
            id = id,
            customername = customername,
            description = description,
            customerCode = customerCode
        )


}
