package com.example.powersync.data.sync.spec

import com.example.powersync.data.local.entity.CustomerEntity

/**
 * Created by H.Mousavioun on 1/31/2026
 */

object CustomersSpec : TableSpec<CustomerEntity> {
    override val type = "customers"
    override val table = "customers"
    override val idColumn = "id"

    override val columns = listOf(
        "customername",
        "description",
        "customercode"
    )

    override fun defaultValue(column: String): Any? = ""

    override fun toMap(entity: CustomerEntity): Map<String, Any?> = mapOf(
        "id" to entity.id,
        "customername" to entity.customername,
        "description" to entity.description,
        "customercode" to entity.customerCode
    )
}