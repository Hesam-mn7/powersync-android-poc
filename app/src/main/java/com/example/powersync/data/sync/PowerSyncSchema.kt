package com.example.powersync.data.sync

import com.example.powersync.data.sync.spec.CustomersSpec
import com.example.powersync.data.sync.spec.ProductsSpec
import com.powersync.ExperimentalPowerSyncAPI
import com.powersync.db.schema.PendingStatement
import com.powersync.db.schema.PendingStatementParameter
import com.powersync.db.schema.RawTable
import com.powersync.db.schema.Schema

/**
 * PowerSync schema for Room integration
 */
@OptIn(ExperimentalPowerSyncAPI::class)
val powerSyncSchema = Schema(
    listOf(
        RawTable(
            name = CustomersSpec.table,
            put = PendingStatement(
                CustomersSpec.putSql(),
                buildList {
                    add(PendingStatementParameter.Id)
                    CustomersSpec.columns.forEach { col ->
                        add(PendingStatementParameter.Column(col))
                    }
                }
            ),
            delete = PendingStatement(
                CustomersSpec.deleteSql(),
                listOf(PendingStatementParameter.Id)
            )
        ),
        RawTable(
            name = ProductsSpec.table,
            put = PendingStatement(
                ProductsSpec.putSql(),
                buildList {
                    add(PendingStatementParameter.Id)
                    ProductsSpec.columns.forEach { col ->
                        add(PendingStatementParameter.Column(col))
                    }
                }
            ),
            delete = PendingStatement(
                ProductsSpec.deleteSql(),
                listOf(PendingStatementParameter.Id)
            )
        )
    )
)