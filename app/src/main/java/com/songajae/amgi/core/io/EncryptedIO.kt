package com.songajae.amgi.core.io

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import java.io.File

object EncryptedIO {
    private fun file(ctx: Context, fileName: String): File = File(ctx.filesDir, fileName)

    private fun encryptedFile(ctx: Context, target: File): EncryptedFile {
        val masterKey = MasterKey.Builder(ctx)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        target.parentFile?.mkdirs()
        return EncryptedFile.Builder(
            ctx,
            target,
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
    }

    fun delete(ctx: Context, fileName: String) {
        runCatching {
            val target = file(ctx, fileName)
            if (target.exists()) {
                target.delete()
            }
        }
    }
    fun writeString(ctx: Context, fileName: String, content: String) {
        runCatching {
            val target = file(ctx, fileName)
            if (target.exists()) target.delete()
            encryptedFile(ctx, target).openFileOutput().use { output ->
                output.write(content.toByteArray(Charsets.UTF_8))
                output.flush()
            }
        }
    }

    fun readString(ctx: Context, fileName: String): String? {
        val target = file(ctx, fileName)
        if (!target.exists()) return null
        return runCatching {
            encryptedFile(ctx, target).openFileInput().use { input ->
                input.readBytes().toString(Charsets.UTF_8)
            }
        }.getOrNull()
    }
}