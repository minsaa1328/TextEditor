package com.example.kotlintexteditor.utils

import android.content.Context
import com.example.kotlintexteditor.model.SyntaxRule
import com.google.gson.Gson

object SyntaxLoader {
    fun loadSyntax(context: Context, fileName: String): SyntaxRule? {
        return try {
            val json = context.assets.open("syntax/$fileName").bufferedReader().use { it.readText() }
            Gson().fromJson(json, SyntaxRule::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
