package com.nullmessenger.shared

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.post
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull

private const val SUPABASE_URL = "https://nlxozatdsstdfckzvmex.supabase.co"
private const val SUPABASE_PUBLISHABLE_KEY = "sb_publishable_tk88inicwpgppVVRlvGv9Q_WyNlAcqE"

object NullAuth {
    @Volatile var accessToken: String? = null
        private set
    @Volatile var refreshToken: String? = null
        private set

    private val client = HttpClient(CIO)

    suspend fun sendEmailCode(email: String) {
        val response = client.post("$SUPABASE_URL/auth/v1/otp") {
            header("apikey", SUPABASE_PUBLISHABLE_KEY)
            header("Authorization", "Bearer $SUPABASE_PUBLISHABLE_KEY")
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject {
                put("email", email)
                put("create_user", true)
            }.toString())
        }
        if (response.status.value !in 200..299) {
            throw IllegalStateException(readAuthError(response.bodyAsText(), "Не удалось отправить письмо."))
        }
    }

    suspend fun verifyEmailCode(email: String, code: String) {
        val response = client.post("$SUPABASE_URL/auth/v1/verify") {
            header("apikey", SUPABASE_PUBLISHABLE_KEY)
            header("Authorization", "Bearer $SUPABASE_PUBLISHABLE_KEY")
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject {
                put("type", "email")
                put("email", email)
                put("token", code)
            }.toString())
        }
        val body = response.bodyAsText()
        if (response.status.value !in 200..299) {
            throw IllegalStateException(readAuthError(body, "Код неверный или истёк."))
        }
        val json = runCatching { Json.parseToJsonElement(body).jsonObject }.getOrNull()
        accessToken = json?.get("access_token")?.jsonPrimitive?.contentOrNull
        refreshToken = json?.get("refresh_token")?.jsonPrimitive?.contentOrNull
        if (accessToken.isNullOrBlank()) {
            throw IllegalStateException("Supabase подтвердил код, но не вернул сессию. Проверь настройки Email Auth.")
        }
    }

    private fun readAuthError(body: String, fallback: String): String {
        val json = runCatching { Json.parseToJsonElement(body).jsonObject }.getOrNull()
        return json?.get("msg")?.jsonPrimitive?.contentOrNull
            ?: json?.get("message")?.jsonPrimitive?.contentOrNull
            ?: json?.get("error_description")?.jsonPrimitive?.contentOrNull
            ?: fallback
    }
}
