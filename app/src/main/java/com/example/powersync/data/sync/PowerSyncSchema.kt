package com.example.powersync.data.sync

import com.powersync.ExperimentalPowerSyncAPI
import com.powersync.db.schema.Schema
import com.powersync.db.schema.RawTable
import com.powersync.db.schema.PendingStatement
import com.powersync.db.schema.PendingStatementParameter

/**
 * Created by H.Mousavioun on 12/16/2025
 */
@ExperimentalPowerSyncAPI
val powerSyncSchema = Schema(
    RawTable(
        name = "customers",
        put = PendingStatement(
            "INSERT OR REPLACE INTO customers (id, customerName, description) VALUES (?, ?, ?)",
            listOf(
                PendingStatementParameter.Id,
                PendingStatementParameter.Column("customerName"),
                PendingStatementParameter.Column("description"),
            )
        ),
        delete = PendingStatement(
            "DELETE FROM customers WHERE id = ?",
            listOf(PendingStatementParameter.Id)
        )
    )
)