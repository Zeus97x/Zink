package eu.kanade.tachiyomi.data.sync.service

import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

internal object CloudBackupCodec {
    fun encode(bytes: ByteArray): String {
        check(bytes.size <= MAX_DECODED_BYTES) { "Backup is too large for cloud sync. Use a local backup." }
        val compressed = ByteArrayOutputStream().also { output ->
            GZIPOutputStream(output).use { it.write(bytes) }
        }.toByteArray()
        check(compressed.size <= MAX_COMPRESSED_BYTES) { "Backup is too large for cloud sync. Use a local backup." }
        return Base64.getEncoder().encodeToString(compressed)
    }

    fun decode(payload: String): ByteArray {
        check(payload.length <= MAX_PAYLOAD_BYTES) { "Remote backup is too large." }
        val compressed = Base64.getDecoder().decode(payload)
        return GZIPInputStream(compressed.inputStream()).use { input ->
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                check(output.size() + count <= MAX_DECODED_BYTES) { "Remote backup is too large." }
                output.write(buffer, 0, count)
            }
            output.toByteArray()
        }
    }

    private const val MAX_COMPRESSED_BYTES = 8 * 1024 * 1024
    private const val MAX_PAYLOAD_BYTES = 12 * 1024 * 1024
    private const val MAX_DECODED_BYTES = 64 * 1024 * 1024
}
