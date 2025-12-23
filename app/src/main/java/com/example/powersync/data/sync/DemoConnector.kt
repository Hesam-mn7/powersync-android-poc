package com.example.powersync.data.sync

import android.util.Log
import com.example.powersync.data.local.CustomerDao
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
    private val customerDao: CustomerDao,
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
        Log.e("PSYNC", "uploadData() CALLED")

        val tx = database.getNextCrudTransaction() ?: return

        val crudArray = JSONArray()

        for (e in tx.crud) {
            val op = e.op.name   // PUT / PATCH / DELETE
            val id = e.id

            val obj = JSONObject()
            obj.put("op", op)
            obj.put("type", "customers")   // ثابت
            obj.put("id", id)

            // ✅ دیتا رو خودمون از Room می‌خونیم
            if (op != "DELETE") {
                val row = customerDao.getById(id)
                if (row != null) {
                    obj.put(
                        "data",
                        JSONObject()
                            .put("id", row.id)
                            .put("customername", row.customername)
                            .put("description", row.description)
                    )
                } else {
                    // اگر رکورد نبود، بیخیال این op می‌شیم
                    Log.e("PSYNC", "uploadData: row not found in local DB for id=$id (op=$op)")
                    continue
                }
            }

            crudArray.put(obj)
        }

        val payload = JSONObject().put("crud", crudArray)
        Log.e("PSYNC", "UPLOAD PAYLOAD = ${payload.toString().take(1500)}")

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

        Log.e("PSYNC", "upload response code=$code body=$resp")

        if (code in 200..299) {
            tx.complete(null) // ✅ خیلی مهم
        } else {
            throw IllegalStateException("Upload failed: HTTP $code body=$resp")
        }
    }
}
