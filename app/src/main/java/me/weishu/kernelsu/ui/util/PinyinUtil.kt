package me.weishu.kernelsu.ui.util

import android.icu.text.Transliterator
import android.os.Build

object PinyinUtil {

    /**
     * `android.icu.text.Transliterator` only exists from API 29, so on older platforms the
     * transliteration is unavailable and the input is returned unchanged.
     */
    private val transliterator: Transliterator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Transliterator.getInstance("Han-Latin; Latin-ASCII; Lower")
        } else {
            null
        }
    }

    @Synchronized
    fun toPinyin(input: String): String {
        val instance = transliterator ?: return input
        return instance.transliterate(input).replace(" ", "")
    }
}
