package com.halalin.util

import android.util.Log
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import coil.api.clear
import coil.api.load
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.halalin.R
import com.halalin.databinding.LayoutStateBinding

private const val LOG_TAG = "log_tag"

fun Any.logd(message: String, throwable: Throwable? = null) {
    Log.d(LOG_TAG, "${this::class.qualifiedName} - $message", throwable)
}

fun Any.loge(message: String, throwable: Throwable? = null) {
    Log.e(LOG_TAG, "${this::class.qualifiedName} - $message", throwable)
}

fun Any.logi(message: String, throwable: Throwable? = null) {
    Log.i(LOG_TAG, "${this::class.qualifiedName} - $message", throwable)
}

fun Any.logw(message: String, throwable: Throwable? = null) {
    Log.w(LOG_TAG, "${this::class.qualifiedName} - $message", throwable)
}

fun String.prettyClassString() =
    trimMargin().replace(Regex(" {4}.*_\\n", RegexOption.MULTILINE), "")

fun <T> List<T>.prettyString(level: Int = 1, indent: String = " ".repeat(4)): String {
    if (isEmpty()) return "[]"
    return joinToString(
        prefix = "[\n",
        separator = ",\n",
        postfix = "\n${indent.repeat(level - 1)}]"
    ) { it.toString().replace(Regex("^", RegexOption.MULTILINE), indent.repeat(level)) }
}

fun TextInputEditText.requireInput(textInputLayout: TextInputLayout): String? {
    val input = text.toString().trim()
    if (input.isEmpty()) textInputLayout.error = context.getString(
        R.string.template_field_required_message,
        (textInputLayout.hint ?: "").toString()
    )
    else textInputLayout.error = null
    return if (input.isEmpty()) null else input
}

fun LayoutStateBinding.setLayout(
    @DrawableRes imageResId: Int,
    @StringRes textResId: Int,
    @StringRes buttonTextResId: Int? = null,
    buttonClickListener: (() -> Unit)? = null
) {
    root.visibility = View.VISIBLE
    imageViewState.load(imageResId)
    textViewState.setText(textResId)
    if (buttonTextResId == null) {
        buttonAction.text = null
        buttonAction.visibility = View.INVISIBLE
    } else {
        buttonAction.setText(buttonTextResId)
        buttonAction.visibility = View.VISIBLE
    }
    if (buttonClickListener != null) buttonAction.setOnClickListener { buttonClickListener() }
}

fun LayoutStateBinding.setEmptyState(
    @StringRes textResId: Int? = null,
    @StringRes buttonTextResId: Int? = null,
    buttonClickListener: (() -> Unit)? = null
) = setLayout(
    R.drawable.svg_undraw_empty_xct9,
    textResId ?: R.string.state_empty_message,
    buttonTextResId,
    buttonClickListener
)

fun LayoutStateBinding.setErrorState(
    @StringRes textResId: Int? = null,
    @StringRes buttonTextResId: Int? = null,
    actionButtonClickListener: (() -> Unit)? = null
) = setLayout(
    R.drawable.svg_undraw_lost_bqr2,
    textResId ?: R.string.state_error_message,
    buttonTextResId,
    actionButtonClickListener
)

fun LayoutStateBinding.setLoadingState() {
    setLayout(R.drawable.svg_undraw_searching_p5ux, R.string.state_loading_message)
}

fun LayoutStateBinding.clearLayout() {
    root.visibility = View.INVISIBLE
    imageViewState.clear()
    textViewState.text = null
    buttonAction.text = null
    buttonAction.setOnClickListener(null)
}
