package com.songajae.amgi.core.license

import android.content.Context
import com.songajae.amgi.data.local.LeaseCache

object LeaseManager {
    private const val DAYS = 14
    suspend fun isLeaseValidOffline(ctx: Context, deviceId: String): Boolean {
        val last = LeaseCache.getLastSeen(ctx, deviceId)
        return last != null && System.currentTimeMillis() <= last + DAYS * 24L * 60 * 60 * 1000
    }
}
