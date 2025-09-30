package com.songajae.amgi.core.net

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object NetworkGate {
    fun isOnline(ctx: Context): Boolean {
        val cm = ctx.getSystemService(ConnectivityManager::class.java)
        val n = cm.activeNetwork ?: return false
        val cap = cm.getNetworkCapabilities(n) ?: return false
        return cap.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
