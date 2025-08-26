package com.example.kotlintexteditor.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.EditText
import androidx.core.content.getSystemService

fun EditText.copy() {
    val cm = context.getSystemService<ClipboardManager>() ?: return
    val selStart = selectionStart.coerceAtLeast(0)
    val selEnd = selectionEnd.coerceAtLeast(0)
    val from = kotlin.math.min(selStart, selEnd)
    val to = kotlin.math.max(selStart, selEnd)
    val text = if (from != to) editableText.substring(from, to) else editableText.toString()
    cm.setPrimaryClip(ClipData.newPlainText("text", text))
}

fun EditText.cut() {
    val cm = context.getSystemService<ClipboardManager>() ?: return
    val selStart = selectionStart.coerceAtLeast(0)
    val selEnd = selectionEnd.coerceAtLeast(0)
    val from = kotlin.math.min(selStart, selEnd)
    val to = kotlin.math.max(selStart, selEnd)
    if (from == to) return
    val text = editableText.substring(from, to)
    cm.setPrimaryClip(ClipData.newPlainText("text", text))
    editableText.delete(from, to)
}

fun EditText.paste() {
    val cm = context.getSystemService<ClipboardManager>() ?: return
    val clip = cm.primaryClip ?: return
    if (clip.itemCount == 0) return
    val pasteText = clip.getItemAt(0).coerceToText(context)
    val selStart = selectionStart.coerceAtLeast(0)
    val selEnd = selectionEnd.coerceAtLeast(0)
    val from = kotlin.math.min(selStart, selEnd)
    val to = kotlin.math.max(selStart, selEnd)
    editableText.replace(from, to, pasteText)
}
