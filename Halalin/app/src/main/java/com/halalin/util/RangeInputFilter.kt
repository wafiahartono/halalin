package com.halalin.util

import android.text.InputFilter
import android.text.Spanned

class RangeInputFilter(
    private val min: Int,
    private val max: Int
) : InputFilter {
    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence {
        val value = dest
            ?.toString()
            ?.replaceRange(
                dstart until dend,
                source?.substring(start until end) ?: ""
            )?.toIntOrNull()
        return when {
            value == null -> ""
            value < min -> ""
            value > max -> ""
            else -> source.toString()
        }
    }
}
