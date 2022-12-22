package com.yausername.youtubedl_common

import android.content.Context

object SharedPrefsHelper {
    private const val sharedPrefsName = "youtubedl-android"
    @JvmStatic
    fun update(appContext: Context, key: String?, value: String?) {
        val pref = appContext.getSharedPreferences(sharedPrefsName, Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putString(key, value)
        editor.apply()
    }

    @JvmStatic
    operator fun get(appContext: Context, key: String?): String? {
        val pref = appContext.getSharedPreferences(sharedPrefsName, Context.MODE_PRIVATE)
        return pref.getString(key, null)
    }
}