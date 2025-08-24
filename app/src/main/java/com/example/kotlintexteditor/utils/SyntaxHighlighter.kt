package com.example.kotlintexteditor.utils


import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan

object SyntaxHighlighter {

    private val keywords = listOf(
        "val", "var", "fun", "if", "else", "when", "for", "while",
        "do", "return", "class", "object", "interface", "package",
        "import", "try", "catch", "finally", "throw", "is", "in", "as", "print"
    )

    fun highlightKotlinCode(code: String): Spannable {
        val spannable = SpannableStringBuilder(code)

        // Highlight keywords
        keywords.forEach { keyword ->
            val regex = "\\b$keyword\\b".toRegex()
            regex.findAll(code).forEach {
                spannable.setSpan(
                    ForegroundColorSpan(Color.parseColor("#FFB300")),  // orange-yellow
                    it.range.first,
                    it.range.last + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        // Highlight comments (single-line)
        "//.*".toRegex().findAll(code).forEach {
            spannable.setSpan(
                ForegroundColorSpan(Color.GRAY),
                it.range.first,
                it.range.last + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        // Highlight multi-line comments
        "/\\*.*?\\*/".toRegex(RegexOption.DOT_MATCHES_ALL).findAll(code).forEach {
            spannable.setSpan(
                ForegroundColorSpan(Color.GRAY),
                it.range.first,
                it.range.last + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        // Highlight strings
        "\".*?\"".toRegex().findAll(code).forEach {
            spannable.setSpan(
                ForegroundColorSpan(Color.GREEN),
                it.range.first,
                it.range.last + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        return spannable
    }
}
