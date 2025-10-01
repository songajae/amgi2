package com.songajae.amgi.core.sync

import android.content.Context
import com.songajae.amgi.R
import com.songajae.amgi.data.packs.ContentPack
import com.songajae.amgi.data.packs.PackRepository
import com.songajae.amgi.data.remote.AdminApi
import com.songajae.amgi.data.remote.DevicePolicyResult
import com.songajae.amgi.util.AppStringProvider
import com.songajae.amgi.util.Result

object SyncManager {
    sealed class SyncResult {
        data class Success(val packs: List<ContentPack>) : SyncResult()
        data class Failure(val message: String) : SyncResult()
        data object DeviceLimitReached : SyncResult()
    }

    suspend fun sync(ctx: Context, deviceId: String): SyncResult {
        return when (val policy = AdminApi.processDevicePolicyAndPing(ctx, deviceId)) {
            DevicePolicyResult.Approved -> when (val remote = PackRepository.fetchRemotePacks()) {
                is Result.Success -> {
                    PackRepository.cachePacks(ctx, remote.data)
                    SyncResult.Success(remote.data)
                }

                is Result.Error -> {
                    val cached = PackRepository.loadCachedPacks(ctx)
                    if (cached.isNotEmpty()) {
                        SyncResult.Success(cached)
                    } else {
                        SyncResult.Failure(remote.message)
                    }
                }

                else -> SyncResult.Failure(
                    AppStringProvider.get(R.string.error_sync_status_unknown)
                )
            }

            DevicePolicyResult.LimitReached -> SyncResult.DeviceLimitReached
            is DevicePolicyResult.Error -> SyncResult.Failure(policy.message)
        }
    }
}