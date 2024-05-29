package com.yausername.youtubedl_common.utils.network

import com.yausername.youtubedl_common.utils.json
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.request.get
import kotlinx.serialization.decodeFromString

object Ktor {
    val client = HttpClient(Android.create {
        connectTimeout = 10_000 //ms
        socketTimeout = 10_000 //ms
    }) {
        //Here we can add proxy support and more things
    }

    @Throws(Exception::class)
    suspend inline fun <reified T> makeApiCall(
        client: HttpClient,
        url: String,
        params: Map<String, String>?
    ): T {
        val response: String = client.get(url) {
            url {
                params?.forEach { (key, value) ->
                    parameters.append(key, value)
                }
            }
        }.body()

        return try {
            json.decodeFromString<T>(response)
        } catch (e: Exception) {
            throw Exception("Failed to parse response: $response", e)
        }
    }
}