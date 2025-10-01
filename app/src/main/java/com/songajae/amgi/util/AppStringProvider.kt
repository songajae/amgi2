package com.songajae.amgi.util

import android.content.Context
import androidx.annotation.StringRes

object AppStringProvider {
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun get(@StringRes resId: Int, vararg args: Any?): String {
        check(::appContext.isInitialized) { "AppStringProvider is not initialized." }
        return if (args.isEmpty()) appContext.getString(resId) else appContext.getString(resId, *args)
    }
}