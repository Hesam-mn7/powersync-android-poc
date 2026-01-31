package com.example.powersync.data.sync.spec

import org.json.JSONObject

/**
 * Created by H.Mousavioun on 1/31/2026
 */

/**
 * Single source of truth for a synced table.
 * Keeps SQL + JSON building consistent and minimizes changes when adding columns.
 */
interface TableSpec<T> {
    val type: String
    val table: String
    val idColumn: String

    /**
     * Column names excluding the id column.
     * The order here is the order used in putSql()/upsertSql() and parameter lists.
     */
    val columns: List<String>

    /**
     * Default value used when a value is missing/null for NOT NULL columns.
     * Override per table if needed.
     */
    fun defaultValue(column: String): Any? = ""

    /**
     * Return a map that includes the id column and all columns defined in [columns].
     * Keys must match database column names.
     */
    fun toMap(entity: T): Map<String, Any?>

    /**
     * Build JSON payload for upload (includes id + all columns).
     * Missing keys will be filled with [defaultValue] for defined columns.
     */
    fun toJson(entity: T): JSONObject {
        val data = JSONObject()

        val map = toMap(entity)

        // Always include id first (if present)
        map[idColumn]?.let { data.put(idColumn, it) }

        // Ensure all defined columns exist in payload
        for (c in columns) {
            val v = map[c]
            data.put(c, v ?: defaultValue(c))
        }

        // If toMap included extra keys (optional), include them too (but don't overwrite)
        for ((k, v) in map) {
            if (k == idColumn) continue
            if (data.has(k)) continue
            data.put(k, v)
        }

        // If id wasn't in map, still ensure it exists (backend expects it)
        if (!data.has(idColumn)) data.put(idColumn, "")

        return data
    }

    /**
     * SQL fragment for Room triggers:
     * json_object('id', NEW.id, 'col1', NEW.col1, ...)
     */
    fun triggerJsonObject(prefix: String = "NEW."): String {
        val pairs = buildList {
            add("'$idColumn', ${prefix}$idColumn")
            for (c in columns) add("'$c', ${prefix}$c")
        }.joinToString(", ")
        return "json_object($pairs)"
    }

    /**
     * INSERT OR REPLACE statement for inbound sync (PowerSync -> Room).
     */
    fun putSql(): String {
        val all = listOf(idColumn) + columns
        val cols = all.joinToString(", ")
        val q = all.joinToString(", ") { "?" }
        return "INSERT OR REPLACE INTO $table ($cols) VALUES ($q)"
    }

    /**
     * DELETE statement for inbound sync (PowerSync -> Room).
     */
    fun deleteSql(): String = "DELETE FROM $table WHERE $idColumn = ?"

    /**
     * Backend upsert SQL for PostgreSQL.
     */
    fun upsertSql(): String {
        val all = listOf(idColumn) + columns
        val cols = all.joinToString(", ")
        val placeholders = all.mapIndexed { idx, _ -> "\$${idx + 1}" }.joinToString(", ")
        val updates = columns.joinToString(",\n  ") { "$it = EXCLUDED.$it" }

        return """
            INSERT INTO $table ($cols)
            VALUES ($placeholders)
            ON CONFLICT ($idColumn) DO UPDATE SET
              $updates
        """.trimIndent()
    }

    /**
     * Values for backend upsert, matching the parameter order of [upsertSql].
     * Uses [defaultValue] when missing/null.
     */
    fun upsertValuesFromMap(map: Map<String, Any?>): List<Any?> {
        val values = ArrayList<Any?>(1 + columns.size)
        values.add(map[idColumn] ?: "")
        for (c in columns) {
            values.add(map[c] ?: defaultValue(c))
        }
        return values
    }

    /**
     * Values for inbound put statement, matching the parameter order of [putSql].
     * Uses [defaultValue] when missing/null.
     */
    fun putValuesFromMap(map: Map<String, Any?>): List<Any?> {
        val values = ArrayList<Any?>(1 + columns.size)
        values.add(map[idColumn] ?: "")
        for (c in columns) {
            values.add(map[c] ?: defaultValue(c))
        }
        return values
    }
}
