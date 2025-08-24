package com.example.kotlintexteditor.utils

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import com.example.kotlintexteditor.model.SyntaxRule

object GenericSyntaxHighlighter {

    fun highlight(code: String, rule: SyntaxRule): Spannable {
        val spannable = SpannableStringBuilder(code)

        // Keywords
        rule.keywords.forEach { keyword ->
            val regex = "\\b$keyword\\b".toRegex()
            regex.findAll(code).forEach {
                spannable.setSpan(
                    ForegroundColorSpan(Color.parseColor("#FF5722")), // Orange
                    it.range.first, it.range.last + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        // Single-line Comments
        if (rule.comment_single.isNotEmpty()) {
            "${Regex.escape(rule.comment_single)}.*".toRegex().findAll(code).forEach {
                spannable.setSpan(
                    ForegroundColorSpan(Color.GRAY),
                    it.range.first, it.range.last + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        // Multi-line Comments
        if (rule.comment_multi_start.isNotEmpty() && rule.comment_multi_end.isNotEmpty()) {
            Regex("${Regex.escape(rule.comment_multi_start)}.*?${Regex.escape(rule.comment_multi_end)}",
                RegexOption.DOT_MATCHES_ALL
            ).findAll(code).forEach {
                spannable.setSpan(
                    ForegroundColorSpan(Color.GRAY),
                    it.range.first, it.range.last + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        // Strings
        if (rule.string_delimiter.isNotEmpty()) {
            Regex("${Regex.escape(rule.string_delimiter)}.*?${Regex.escape(rule.string_delimiter)}").findAll(code).forEach {
                spannable.setSpan(
                    ForegroundColorSpan(Color.parseColor("#4CAF50")), // Green
                    it.range.first, it.range.last + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        return spannable
    }
}
