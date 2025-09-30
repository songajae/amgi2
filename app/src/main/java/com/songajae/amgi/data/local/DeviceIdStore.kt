package com.songajae.amgi.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.util.UUID

object DeviceIdStore {
    private const val PREF = "secure_prefs"
    private const val KEY = "device_id"

    private fun sp(ctx: Context) = EncryptedSharedPreferences.create(
        PREF, MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        ctx,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun peek(ctx: Context): String? = sp(ctx).getString(KEY, null)

    fun getOrCreate(ctx: Context): String {
        peek(ctx)?.let { return it }
        val id = UUID.randomUUID().toString()
        sp(ctx).edit().putString(KEY, id).apply()
        return id
    }
}
