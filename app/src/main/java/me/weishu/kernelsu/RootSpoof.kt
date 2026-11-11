package me.weishu.kernelsu

import android.content.Context

/**
 * App-level fake root state.
 *
 * Nothing here grants real root. It only makes the manager *report* a rooted device so the
 * UI shows the "root obtained" state. The flag lives in app-private preferences, so it is
 * gone when the app is uninstalled and a fresh install has to jailbreak again.
 */
object RootSpoof {
    private const val PREFS = "settings"
    private const val KEY = "root_spoofed"

    fun isEnabled(): Boolean = runCatching {
        ksuApp.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY, false)
    }.getOrDefault(false)

    fun setEnabled(enabled: Boolean) {
        runCatching {
            ksuApp.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putBoolean(KEY, enabled)
                .apply()
        }
        runCatching { Natives.setRootSpoofedNative(enabled, BuildConfig.VERSION_CODE) }
    }
}
