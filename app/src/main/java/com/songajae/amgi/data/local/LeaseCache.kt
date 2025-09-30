package com.songajae.amgi.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.ds by preferencesDataStore(name = "lease_cache")

object LeaseCache {
    private fun key(deviceId: String) = longPreferencesKey("lastSeenAt_$deviceId")
    suspend fun save(ctx: Context, deviceId: String, lastSeenAtMs: Long) {
        ctx.ds.edit { it[key(deviceId)] = lastSeenAtMs }
    }
    suspend fun getLastSeen(ctx: Context, deviceId: String): Long? {
        return ctx.ds.data.first()[key(deviceId)]
    }
}
