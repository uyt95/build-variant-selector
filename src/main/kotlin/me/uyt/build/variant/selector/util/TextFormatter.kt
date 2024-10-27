package me.uyt.build.variant.selector.util

import java.util.*

object TextFormatter {
    fun capitalize(text: String): String = text.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }
}
