package com.example.powersync.data.sync.spec

/**
 * Created by H.Mousavioun on 2/1/2026
 */
object ProductsSpec : TableSpec<Any> {
    override val type: String = "products"
    override val table: String = "products"
    override val idColumn: String = "id"

    override val columns: List<String> = listOf(
        "productname",
        "productcode"
    )

    override fun toMap(entity: Any): Map<String, Any?> {
        throw UnsupportedOperationException("Not used in this project")
    }
}