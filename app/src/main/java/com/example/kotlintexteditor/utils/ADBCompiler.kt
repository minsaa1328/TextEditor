package com.example.kotlintexteditor.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import java.io.File

object ADBCompiler {
    fun compile(context: Context, code: String, callback: (String, String) -> Unit) {
        val dir = context.getExternalFilesDir(null)!!
        val trigger = File(dir, "compile.kt")
        val compilerFile = File(dir, "compiler_output.txt")
        val runtimeFile = File(dir, "runtime_output.txt")

        // 1) Clear any stale outputs BEFORE we start polling
        if (compilerFile.exists()) compilerFile.delete()
        if (runtimeFile.exists())  runtimeFile.delete()

        // 2) Force-recreate the trigger file so the PC listener always sees a change
        if (trigger.exists()) trigger.delete()
        try {
            trigger.writeText(code)
        } catch (e: Exception) {
            callback("Failed to write compile.kt: ${e.message}", "")
            return
        }

        // 3) Poll for results (up to 30s)
        val start = System.currentTimeMillis()
        val timeoutMs = 30_000L
        val pollMs = 800L
        val handler = Handler(Looper.getMainLooper())

        fun poll() {
            val haveCompiler = compilerFile.exists()
            val haveRuntime  = runtimeFile.exists()
            if (haveCompiler || haveRuntime) {
                val compilerOut = if (haveCompiler) compilerFile.readText() else "No compiler output found."
                val runtimeOut  = if (haveRuntime) runtimeFile.readText() else "No runtime output found."
                callback(compilerOut, runtimeOut)
            } else if (System.currentTimeMillis() - start < timeoutMs) {
                handler.postDelayed({ poll() }, pollMs)
            } else {
                callback("Timed out waiting for results.", "")
            }
        }
        poll()
    }
}
