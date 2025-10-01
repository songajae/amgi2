package com.songajae.amgi.core.net

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object NetworkGate {
    fun isOnline(ctx: Context): Boolean {
        val cm = ctx.getSystemService(ConnectivityManager::class.java)
            ?: return false
        val n = runCatching { cm.activeNetwork }.getOrNull() ?: return false
        val cap = runCatching { cm.getNetworkCapabilities(n) }.getOrNull() ?: return false
        return cap.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
