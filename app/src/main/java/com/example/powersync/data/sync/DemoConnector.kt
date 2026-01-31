package com.example.powersync.data.sync

import android.util.Log
import com.powersync.PowerSyncDatabase
import com.powersync.connectors.PowerSyncBackendConnector
import com.powersync.connectors.PowerSyncCredentials
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Created by H.Mousavioun on 12/16/2025
 */
class DemoConnector(
    private val hostIp: String,
) : PowerSyncBackendConnector() {

    private val backendBase = "http://$hostIp:6060"
    private val powerSyncEndpoint = "http://$hostIp:8080"
    private val sub = "demo"

    override suspend fun fetchCredentials(): PowerSyncCredentials {
        Log.e("PSYNC", "fetchCredentials() START base=$backendBase endpoint=$powerSyncEndpoint")

        val conn = (URL("$backendBase/api/auth/token?sub=$sub").openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8_000
            readTimeout = 8_000
        }

        val code = conn.responseCode
        val body = (if (code in 200..299) conn.inputStream else conn.errorStream)
            .bufferedReader().readText()

        Log.e("PSYNC", "fetchCredentials() HTTP=$code body=$body")

        val token = JSONObject(body).getString("token")
        return PowerSyncCredentials(endpoint = powerSyncEndpoint, token = token)
    }

    override suspend fun uploadData(database: PowerSyncDatabase) {
        val tx = database.getNextCrudTransaction() ?: return

        val crudArray = JSONArray()

        for (e in tx.crud) {
            val op = e.op.name
            val id = e.id
            val table = e.table

            val obj = JSONObject()
                .put("op", op)
                .put("id", id)
                .put("type", table)

            if (op != "DELETE") {
                val dataObj = JSONObject().put("id", id)

                val opData = e.opData
                if (opData != null) {
                    putOpDataIntoJson(opData, dataObj)
                }

                obj.put("data", dataObj)
            }

            crudArray.put(obj)
        }

        if (crudArray.length() == 0) {
            tx.complete(null)
            return
        }

        val payload = JSONObject().put("crud", crudArray)

        val conn = (URL("$backendBase/api/upload").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            doOutput = true
            connectTimeout = 10_000
            readTimeout = 10_000
        }

        conn.outputStream.use { it.write(payload.toString().toByteArray()) }

        val code = conn.responseCode
        val resp = (if (code in 200..299) conn.inputStream else conn.errorStream)
            ?.bufferedReader()?.readText()

        if (code in 200..299) {
            tx.complete(null)
        } else {
            throw IllegalStateException("Upload failed: HTTP $code body=$resp")
        }
    }

    private fun putOpDataIntoJson(opData: Any, target: JSONObject) {
        when (opData) {
            is Map<*, *> -> {
                for ((k, v) in opData.entries) {
                    val key = k?.toString() ?: continue
                    target.put(key, v)
                }
                return
            }

            is JSONObject -> {
                val keys = opData.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    target.put(key, opData.opt(key))
                }
                return
            }
        }

        // Reflection fallback for different SDK shapes
        runCatching {
            val m = opData.javaClass.methods.firstOrNull { it.name == "toMap" && it.parameterCount == 0 }
                ?.invoke(opData)
            if (m is Map<*, *>) {
                for ((k, v) in m.entries) {
                    val key = k?.toString() ?: continue
                    target.put(key, v)
                }
                return
            }
        }

        runCatching {
            val m = opData.javaClass.methods.firstOrNull { it.name == "asMap" && it.parameterCount == 0 }
                ?.invoke(opData)
            if (m is Map<*, *>) {
                for ((k, v) in m.entries) {
                    val key = k?.toString() ?: continue
                    target.put(key, v)
                }
                return
            }
        }

        runCatching {
            val keySet = opData.javaClass.methods.firstOrNull { it.name == "keySet" && it.parameterCount == 0 }
                ?.invoke(opData)
            if (keySet is Set<*>) {
                val getter = opData.javaClass.methods.firstOrNull { it.name == "get" && it.parameterCount == 1 }
                for (k in keySet) {
                    if (k == null) continue
                    val key = k.toString()
                    val value = getter?.invoke(opData, k)
                    target.put(key, value)
                }
                return
            }
        }

        runCatching {
            val keysMethod = opData.javaClass.methods.firstOrNull { it.name == "keys" && it.parameterCount == 0 }
            val keysObj = keysMethod?.invoke(opData) ?: return
            val iterator: Iterator<Any?> = when (keysObj) {
                is Iterator<*> -> keysObj as Iterator<Any?>
                is java.util.Enumeration<*> -> object : Iterator<Any?> {
                    override fun hasNext(): Boolean = keysObj.hasMoreElements()
                    override fun next(): Any? = keysObj.nextElement()
                }
                else -> return
            }

            val getter = opData.javaClass.methods.firstOrNull { it.name == "get" && it.parameterCount == 1 }
            while (iterator.hasNext()) {
                val k = iterator.next() ?: continue
                val key = k.toString()
                val value = getter?.invoke(opData, k)
                target.put(key, value)
            }
        }
    }
}
