package eu.kanade.tachiyomi.data.sync.service

import eu.kanade.domain.sync.SyncPreferences
import eu.kanade.tachiyomi.network.await
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/** Same account backend as the other ZApps; passwords are never saved. */
class ZAppsAccount(private val preferences: SyncPreferences) {
    private val json = Json { ignoreUnknownKeys = true }

    @Serializable
    data class User(val id: String, val email: String = "")

    @Serializable
    data class Session(
        @SerialName("access_token") val accessToken: String,
        @SerialName("refresh_token") val refreshToken: String,
        @SerialName("expires_at") val expiresAt: Long = 0,
        @SerialName("expires_in") val expiresIn: Long = 3600,
        val user: User,
    )

    fun savedSession(): Session? = runCatching {
        json.decodeFromString(Session.serializer(), preferences.zAppsSession.get())
    }.getOrNull()

    suspend fun signIn(email: String, password: String): Session = sessionMutex.withLock {
        val payload = buildJsonObject {
            put("email", email.trim())
            put("password", password)
        }
        saveSession(tokenRequest("password", payload.toString()))
    }

    suspend fun validSession(): Session = sessionMutex.withLock {
        val session = savedSession() ?: error("Sign in to your ZApps account in Settings first.")
        if (session.expiresAt > System.currentTimeMillis() / 1000 + 60) return@withLock session
        val payload = buildJsonObject { put("refresh_token", session.refreshToken) }
        saveSession(tokenRequest("refresh_token", payload.toString()))
    }

    suspend fun signOut() = sessionMutex.withLock {
        val session = savedSession()
        try {
            if (session != null) {
                val request = Request.Builder().url("$URL/auth/v1/logout?scope=local")
                    .header("apikey", PUBLISHABLE_KEY)
                    .header("Authorization", "Bearer ${session.accessToken}")
                    .post(ByteArray(0).toRequestBody())
                    .build()
                client.newCall(request).await().use { /* Always discard this device's session. */ }
            }
        } finally {
            preferences.zAppsSession.set("")
            preferences.lastSyncTimestamp.set(0)
        }
    }

    private suspend fun tokenRequest(grant: String, payload: String): Session {
        val request = Request.Builder().url("$URL/auth/v1/token?grant_type=$grant")
            .header("apikey", PUBLISHABLE_KEY)
            .post(payload.toRequestBody("application/json".toMediaType()))
            .build()
        return client.newCall(request).await().use { response ->
            check(response.isSuccessful) {
                when (response.code) {
                    400, 401, 403, 422 -> "Sign-in failed. Check your email and password, or sign in again."
                    429 -> "Too many sign-in attempts. Try again later."
                    else -> "ZApps account service is unavailable (HTTP ${response.code})."
                }
            }
            json.decodeFromString(Session.serializer(), response.body.string())
        }
    }

    private fun saveSession(session: Session): Session {
        val previousUser = savedSession()?.user?.id
        val saved = session.copy(expiresAt = System.currentTimeMillis() / 1000 + session.expiresIn)
        // One preference write stores rotated tokens together; no tokens enter backups.
        preferences.zAppsSession.set(json.encodeToString(Session.serializer(), saved))
        if (previousUser != saved.user.id) preferences.lastSyncTimestamp.set(0)
        return saved
    }

    companion object {
        const val URL = "https://sihbduemrgwqdznkxykx.supabase.co"

        // Public client key, scoped by authenticated owner RLS. Never use a secret/service key here.
        const val PUBLISHABLE_KEY = "sb_publishable_jrw7-adGzT_mwZctDRwVnQ_faWEyuF9"
        private val sessionMutex = Mutex()

        // Dedicated client avoids the app's debug header logger exposing bearer tokens.
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }
}
