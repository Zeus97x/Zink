package eu.kanade.tachiyomi.data.sync.service

import android.content.Context
import eu.kanade.domain.sync.SyncPreferences
import eu.kanade.tachiyomi.data.backup.models.Backup
import eu.kanade.tachiyomi.network.await
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.protobuf.ProtoBuf
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class SupabaseSyncService(context: Context, json: Json, syncPreferences: SyncPreferences) :
    SyncService(context, json, syncPreferences) {
    private val account = ZAppsAccount(syncPreferences)
    private val protoBuf: ProtoBuf = Injekt.get()

    @Serializable
    private data class Snapshot(
        val revision: Long,
        val payload: String,
        @SerialName("device_id") val deviceId: String,
    )

    override suspend fun doSync(syncData: SyncData): Backup? {
        // Reject corrupt/unsupported remote data rather than replacing it with local data.
        val session = account.validSession()
        repeat(3) {
            val remote = pull(session)
            val merged = if (remote == null) {
                syncData.copy(backup = syncData.backup?.let(LibraryReadingBackup::restrict))
            } else {
                val bytes = CloudBackupCodec.decode(remote.payload)
                val backup = protoBuf.decodeFromByteArray(Backup.serializer(), bytes)
                syncData.copy(backup = LibraryReadingBackup.merge(requireNotNull(syncData.backup), backup))
            }
            val backup = merged.backup ?: return null
            val payload = CloudBackupCodec.encode(protoBuf.encodeToByteArray(Backup.serializer(), backup))
            if (commit(session, remote?.revision ?: 0, payload)) return backup
            // Another device won the revision race. Pull and merge its latest snapshot.
        }
        error("Another device is syncing. Your local data is safe; try sync again.")
    }

    private suspend fun pull(session: ZAppsAccount.Session): Snapshot? {
        val url = "${ZAppsAccount.URL}/rest/v1/zink_sync?select=revision,payload,device_id&user_id=eq.${session.user.id}"
        val request = request(session).url(url).get().build()
        return ZAppsAccount.client.newCall(request).await().use { response ->
            checkResponse(response.code, response.isSuccessful)
            val snapshots = json.decodeFromString<List<Snapshot>>(response.body.string())
            check(snapshots.size <= 1) { "Invalid cloud sync response. Your local library is unchanged." }
            snapshots.singleOrNull()
        }
    }

    private suspend fun commit(session: ZAppsAccount.Session, revision: Long, payload: String): Boolean {
        val body = buildJsonObject {
            put("expected_revision", revision)
            put("new_payload", payload)
            put("new_device_id", syncPreferences.uniqueDeviceID())
        }.toString().toRequestBody("application/json".toMediaType())
        val request = request(session).url("${ZAppsAccount.URL}/rest/v1/rpc/zink_sync_commit")
            .post(body).build()
        return ZAppsAccount.client.newCall(request).await().use { response ->
            checkResponse(response.code, response.isSuccessful)
            response.body.string().trim().toLong() > 0
        }
    }

    private fun request(session: ZAppsAccount.Session): Request.Builder = Request.Builder()
        .header("apikey", ZAppsAccount.PUBLISHABLE_KEY)
        .header("Authorization", "Bearer ${session.accessToken}")
        .header("Cache-Control", "no-store")

    private fun checkResponse(code: Int, successful: Boolean) {
        check(successful) {
            when (code) {
                401, 403 -> "Cloud sync access denied. Sign in again or check account permissions."
                404 -> "ZInk cloud sync has not been enabled on the server yet."
                413 -> "This backup is too large for cloud sync. Use a local backup."
                429 -> "Cloud sync is busy. Try again later."
                else -> "Cloud sync failed (HTTP $code). Your local library is unchanged."
            }
        }
    }

}
