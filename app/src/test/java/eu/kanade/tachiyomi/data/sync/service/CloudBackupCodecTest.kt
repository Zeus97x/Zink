package eu.kanade.tachiyomi.data.sync.service

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.Base64

class CloudBackupCodecTest {
    @Test
    fun roundTripsBinaryBackupIncludingZeroBytes() {
        val data = ByteArray(65536) { (it % 256).toByte() }
        assertArrayEquals(data, CloudBackupCodec.decode(CloudBackupCodec.encode(data)))
    }

    @Test
    fun rejectsCorruptOrWrongFormatDataRatherThanOverwritingIt() {
        assertThrows(IllegalArgumentException::class.java) { CloudBackupCodec.decode("not base64!") }
        val wrongFormat = Base64.getEncoder().encodeToString(byteArrayOf(1, 2, 3, 4))
        assertThrows(java.util.zip.ZipException::class.java) { CloudBackupCodec.decode(wrongFormat) }
    }

    @Test
    fun boundsExpandedRemoteBackupEvenWhenItCompressesVeryWell() {
        val bytes = ByteArray(64 * 1024 * 1024 + 1)
        assertThrows(IllegalStateException::class.java) { CloudBackupCodec.encode(bytes) }
        val output = java.io.ByteArrayOutputStream()
        java.util.zip.GZIPOutputStream(output).use { it.write(bytes) }
        val payload = Base64.getEncoder().encodeToString(output.toByteArray())
        assertThrows(IllegalStateException::class.java) { CloudBackupCodec.decode(payload) }
    }
}
