package com.example.powersync.data.sync

import com.powersync.ExperimentalPowerSyncAPI
import com.powersync.db.schema.Schema
import com.powersync.db.schema.RawTable
import com.powersync.db.schema.PendingStatement
import com.powersync.db.schema.PendingStatementParameter

/**
 * PowerSync schema for Room integration
 * NO RawTable on Android
 */
@OptIn(ExperimentalPowerSyncAPI::class)
val powerSyncSchema = Schema(
    listOf(
        RawTable(
            name = "customers",
            put = PendingStatement(
                "INSERT OR REPLACE INTO customers (id, customername, description, customerCode) VALUES (?, ?, ?)",
                listOf(
                    PendingStatementParameter.Id,
                    PendingStatementParameter.Column("customername"),
                    PendingStatementParameter.Column("description"),
                    PendingStatementParameter.Column("customerCode"),
                )
            ),
            delete = PendingStatement(
                "DELETE FROM customers WHERE id = ?",
                listOf(PendingStatementParameter.Id)
            )
        )
    )
)