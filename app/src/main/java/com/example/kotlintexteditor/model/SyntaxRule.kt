package com.example.kotlintexteditor.model

data class SyntaxRule(
    val language: String,
    val keywords: List<String>,
    val comment_single: String,
    val comment_multi_start: String,
    val comment_multi_end: String,
    val string_delimiter: String
)
