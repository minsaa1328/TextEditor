package com.example.kotlintexteditor

import UndoHelper
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import com.example.kotlintexteditor.model.SyntaxRule
import com.example.kotlintexteditor.utils.*
import kotlinx.coroutines.*
import java.io.*

class MainActivity : AppCompatActivity() {

    private var isHighlighting = false

    private lateinit var editor: EditText
    private lateinit var lineNumbers: TextView
    private lateinit var statusBar: TextView
    private lateinit var languageSpinner: Spinner
    private lateinit var undoHelper: UndoHelper
    private lateinit var tvFileName: TextView
    private var currentFileName = "untitled.txt"
    private val OPEN_FILE_REQUEST_CODE = 100
    private lateinit var fileListAdapter: ArrayAdapter<String>
    private var currentSyntaxRule: SyntaxRule? = null

    private val handler = Handler(Looper.getMainLooper())
    private val debounceRunnable = Runnable {
        highlightEditor()
        updateStatus()
        updateLineNumbers()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editor = findViewById(R.id.editor)
        lineNumbers = findViewById(R.id.lineNumbers)
        statusBar = findViewById<TextView>(R.id.statusCounts) ?: run {
            Log.e("MainActivity", "statusCounts view not found with ID R.id.statusCounts")
            throw IllegalStateException("statusCounts TextView not found in layout")
        }
        languageSpinner = findViewById(R.id.languageSpinner)

        undoHelper = UndoHelper(editor)
        undoHelper.start()

        // Simple focus
        editor.requestFocus()

        tvFileName = findViewById(R.id.tvTitle)
        // Focus and show keyboard
        fun focusEditor() {
            editor.requestFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editor, InputMethodManager.SHOW_IMPLICIT)
        }

        // Set up tab switching
        findViewById<Button>(R.id.tabCompiling).setOnClickListener {
            switchTab(true)
        }

        findViewById<Button>(R.id.tabOutput).setOnClickListener {
            switchTab(false)
        }


        // Check if we're opening an existing file from HomeActivity
        val fileName = intent.getStringExtra("OPEN_FILE_NAME")
        if (!fileName.isNullOrEmpty()) {
            currentFileName = fileName
            openInternalFile(fileName)
        } else {
            // If no file specified, check if we have a saved state
            if (savedInstanceState != null) {
                currentFileName = savedInstanceState.getString("currentFileName", "untitled.txt")
            }
            updateStatus()
        }



        findViewById<EditText>(R.id.editor).setOnClickListener {
            Log.d("EditText", "Editor clicked")
        }
        // findViewById<ImageButton>(R.id.btnNew).setOnClickListener { newFile() }
        //findViewById<ImageButton>(R.id.btnOpen).setOnClickListener { showFilePickerDialog() }
        findViewById<ImageButton>(R.id.btnSave).setOnClickListener { saveFile() }
        findViewById<ImageButton>(R.id.btnCompile).setOnClickListener { compileCode() }

        val btnMenu = findViewById<ImageButton>(R.id.btnMenu)
        btnMenu.setOnClickListener { showPopupMenu(btnMenu) }

        // Set up additional buttons from the layout
        findViewById<Button>(R.id.btnUndo).setOnClickListener { undoHelper.undo() }
        findViewById<Button>(R.id.btnRedo).setOnClickListener { undoHelper.redo() }
        findViewById<Button>(R.id.btnReplace).setOnClickListener { showFindReplaceDialog() }

        val languages = arrayOf("Kotlin", "Java", "Python")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, languages)
        languageSpinner.adapter = adapter

        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedLang = parent.getItemAtPosition(position).toString().lowercase()
                currentSyntaxRule = if (selectedLang == "kotlin") null
                else SyntaxLoader.loadSyntax(this@MainActivity, "$selectedLang.json")
                highlightEditor()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        editor.addTextChangedListener(object : TextWatcher {
            private var previousText = editor.text.toString()

            override fun afterTextChanged(s: Editable?) {
                if (undoHelper.isProgrammaticChange) return

                val currentText = s?.toString() ?: ""
                if (currentText != previousText) {
                    handler.removeCallbacks(debounceRunnable)
                    handler.postDelayed(debounceRunnable, 300)
                    previousText = currentText
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        )

        editor.addTextChangedListener(object : TextWatcher {
            private var previousText = editor.text.toString()

            override fun afterTextChanged(s: Editable?) {
                if (undoHelper.isProgrammaticChange) return

                val currentText = s?.toString() ?: ""
                if (currentText != previousText) {
                    handler.removeCallbacks(debounceRunnable)
                    handler.postDelayed(debounceRunnable, 300)
                    previousText = currentText
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })


        // Set up copy/paste buttons
        findViewById<Button>(R.id.btnCopy).setOnClickListener {
            val selection = editor.selectionStart to editor.selectionEnd
            if (selection.first != selection.second) {
                val selectedText = editor.text.substring(selection.first, selection.second)
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("code", selectedText)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No text selected", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btnPaste).setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            if (clipboard.hasPrimaryClip()) {
                val clip = clipboard.primaryClip
                if (clip != null && clip.itemCount > 0) {
                    val text = clip.getItemAt(0).text.toString()
                    val start = editor.selectionStart
                    val end = editor.selectionEnd
                    editor.text.replace(start, end, text)
                    editor.setSelection(start + text.length)
                }
            } else {
                Toast.makeText(this, "Clipboard is empty", Toast.LENGTH_SHORT).show()
            }
        }


        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            // Always go to HomeActivity
            val intent = Intent(this, HomeActivity::class.java).apply {
                // If Home is already in the stack, reuse it
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            startActivity(intent)
            finish() // remove this editor screen from the back stack
        }

        findViewById<Button>(R.id.btnCut).setOnClickListener {
            val selection = editor.selectionStart to editor.selectionEnd
            if (selection.first != selection.second) {
                val selectedText = editor.text.substring(selection.first, selection.second)
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("code", selectedText)
                clipboard.setPrimaryClip(clip)

                // Remove the selected text
                editor.text.delete(selection.first, selection.second)
                Toast.makeText(this, "Cut to clipboard", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No text selected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun compileCode() {
        val cleanCode = editor.text.toString().trim()

        // Get references to the output views in the bottom panel
        val compileLog = findViewById<TextView>(R.id.compileLog)
        val runtimeOutput = findViewById<TextView>(R.id.runtimeOutput)

        // Clear previous output and show compiling message
        compileLog.text = "--- Compiling ---"
        runtimeOutput.text = ""

        // Switch to the compiling tab
        switchTab(true)

        ADBCompiler.compile(this, cleanCode) { compilerOutput, runtimeOutputText ->
            runOnUiThread {
                // Update compiler output tab
                compileLog.text = "--- Compiling ---\n$compilerOutput"

                // Update runtime output tab
                runtimeOutput.text = if (runtimeOutputText.isNotEmpty()) {
                    runtimeOutputText
                } else {
                    "No runtime output"
                }

                Toast.makeText(this, "Compilation and execution complete", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun switchTab(showCompiling: Boolean) {
        val compileLog = findViewById<TextView>(R.id.compileLog)
        val runtimeOutput = findViewById<TextView>(R.id.runtimeOutput)
        val tabCompiling = findViewById<Button>(R.id.tabCompiling)
        val tabOutput = findViewById<Button>(R.id.tabOutput)

        if (showCompiling) {
            compileLog.visibility = View.VISIBLE
            runtimeOutput.visibility = View.GONE
            tabCompiling.isSelected = true
            tabOutput.isSelected = false
            // You might want to change background colors to indicate active tab
            tabCompiling.setBackgroundColor(ContextCompat.getColor(this, R.color.bg_card_dark))
            tabOutput.setBackgroundColor(ContextCompat.getColor(this, R.color.bg_appbar_dark))
        } else {
            compileLog.visibility = View.GONE
            runtimeOutput.visibility = View.VISIBLE
            tabCompiling.isSelected = false
            tabOutput.isSelected = true
            // Change background colors for active tab indication
            tabCompiling.setBackgroundColor(ContextCompat.getColor(this, R.color.bg_appbar_dark))
            tabOutput.setBackgroundColor(ContextCompat.getColor(this, R.color.bg_card_dark))
        }
    }

    private fun highlightEditor() {
        if (isHighlighting) return

        isHighlighting = true

        val code = editor.text.toString()
        val selectionStart = editor.selectionStart
        val selectionEnd = editor.selectionEnd

        // Store current text for comparison (without spans)
        val currentPlainText = editor.text.toString()

        CoroutineScope(Dispatchers.Default).launch {
            val highlighted = try {
                if (currentSyntaxRule == null) {
                    SyntaxHighlighter.highlightKotlinCode(code)
                } else {
                    GenericSyntaxHighlighter.highlight(code, currentSyntaxRule!!)
                }
            } catch (e: Exception) {
                Log.e("SyntaxHighlight", "Error during highlighting", e)
                SpannableString(code) // Fallback to plain text
            }

            withContext(Dispatchers.Main) {
                // Only update if the plain text content hasn't changed during highlighting
                val currentTextAfterDelay = editor.text.toString()
                if (currentPlainText == currentTextAfterDelay) {
                    undoHelper.setTextProgrammatically(highlighted, selectionStart, selectionEnd)
                }
                isHighlighting = false
            }
        }
    }

    private fun updateLineNumbers() {
        val lines = editor.lineCount
        val sb = StringBuilder()
        for (i in 1..lines) sb.append("$i\n")
        lineNumbers.text = sb.toString()
    }

    private fun updateStatus() {
        if (!::statusBar.isInitialized) {
            Log.w("MainActivity", "statusBar not initialized, skipping update")
            return
        }

        val textContent = editor.text.toString()
        val wordCount = textContent.split("\\s+".toRegex())
            .filter { it.isNotEmpty() }
            .size
        val charCount = textContent.length

        statusBar.text = "$currentFileName | Words: $wordCount | Chars: $charCount"
        tvFileName.text = currentFileName
    }
    private fun newFile() {
        editor.setText("")
        currentFileName = "untitled.txt"
        Toast.makeText(this, "New file created", Toast.LENGTH_SHORT).show()
        updateStatus()
    }

    private fun saveFile() {
        try {
            val file = File(filesDir, currentFileName)
            val writer = FileWriter(file)
            writer.write(editor.text.toString())
            writer.close()
            Toast.makeText(this, "File saved: $currentFileName", Toast.LENGTH_SHORT).show()
            updateStatus() // Update status to reflect the saved file name
            // Update the top title to reflect the saved file name
            setTitle("$currentFileName Saved")
        } catch (e: IOException) {
            Toast.makeText(this, "Error saving file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPopupMenu(anchor: ImageButton) {
        val popup = PopupMenu(this, anchor)
        popup.menu.add(0, 1, 0, "Save As")
        popup.menu.add(0, 2, 1, "Undo")
        popup.menu.add(0, 3, 2, "Redo")
        popup.menu.add(0, 4, 3, "Find & Replace")

        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                1 -> showSaveAsDialog()
                2 -> undoHelper.undo()
                3 -> undoHelper.redo()
                4 -> showFindReplaceDialog()
            }
            true
        }
        popup.show()
    }

    private fun showSaveAsDialog() {
        val input = EditText(this)
        input.hint = "Enter filename (e.g., myfile.txt)"

        AlertDialog.Builder(this)
            .setTitle("Save As")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val fileName = input.text.toString().trim()
                if (fileName.isNotEmpty()) {
                    currentFileName = fileName
                    saveFile()
                } else {
                    Toast.makeText(this, "Filename cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showFindReplaceDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 0)
        }

        val inputFind = EditText(this).apply { hint = "Find" }
        val inputReplace = EditText(this).apply { hint = "Replace with" }
        val matchCase = CheckBox(this).apply { text = "Match Case" }
        val wholeWord = CheckBox(this).apply { text = "Whole Word" }

        layout.addView(inputFind)
        layout.addView(inputReplace)
        layout.addView(matchCase)
        layout.addView(wholeWord)

        AlertDialog.Builder(this)
            .setTitle("Find and Replace")
            .setView(layout)
            .setPositiveButton("Replace") { _, _ ->
                replaceText(inputFind.text.toString(), inputReplace.text.toString(), matchCase.isChecked, wholeWord.isChecked, false)
            }
            .setNeutralButton("Replace All") { _, _ ->
                replaceText(inputFind.text.toString(), inputReplace.text.toString(), matchCase.isChecked, wholeWord.isChecked, true)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun replaceText(target: String, replacement: String, matchCase: Boolean, wholeWord: Boolean, replaceAll: Boolean) {
        if (target.isEmpty()) return

        var text = editor.text.toString()
        var pattern = Regex.escape(target)
        if (wholeWord) pattern = "\\b$pattern\\b"
        val flags = if (!matchCase) setOf(RegexOption.IGNORE_CASE) else emptySet()
        val regex = Regex(pattern, flags)

        if (replaceAll) {
            val newText = regex.replace(text, replacement)
            editor.setText(newText)
        } else {
            val match = regex.find(text)
            if (match != null) {
                val start = match.range.first
                val end = match.range.last + 1
                editor.text.replace(start, end, replacement)
                editor.setSelection(start + replacement.length)
            } else {
                Toast.makeText(this, "No match found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun listInternalFiles(): List<String> {
        return filesDir.listFiles()?.map { it.name } ?: emptyList()
    }

    private fun showFilePickerDialog() {
        val context = this
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 0)
        }
        val title = TextView(context).apply {
            text = "Select a file to open"
            textSize = 18f
            setPadding(0, 0, 0, 20)
        }
        val listView = ListView(context).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 600)
        }
        val fileList = listInternalFiles()
        fileListAdapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, fileList)
        listView.adapter = fileListAdapter

        container.addView(title)
        container.addView(listView)

        val dialog = AlertDialog.Builder(context)
            .setView(container)
            .setNegativeButton("Cancel", null)
            .create()

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedFile = fileList[position]
            currentFileName = selectedFile
            openInternalFile(selectedFile)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun openInternalFile(fileName: String) {
        try {
            val file = File(filesDir, fileName)
            val reader = BufferedReader(FileReader(file))
            val text = reader.readText()
            reader.close()
            editor.setText(text)
            Toast.makeText(this, "Opened: $fileName", Toast.LENGTH_SHORT).show()
            updateStatus()
        } catch (e: IOException) {
            Toast.makeText(this, "Error opening file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openFileWithPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/*"
        }
        startActivityForResult(intent, OPEN_FILE_REQUEST_CODE)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == OPEN_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.also { uri ->
                readTextFromUri(uri)?.let { text ->
                    editor.setText(text)
                    currentFileName = uri.lastPathSegment ?: "OpenedFile.txt"
                    Toast.makeText(this, "Opened: $currentFileName", Toast.LENGTH_SHORT).show()
                    updateStatus()
                } ?: Toast.makeText(this, "Error reading file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun readTextFromUri(uri: Uri): String? {
        return try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).readText()
            }
        } catch (e: IOException) {
            null
        }
    }

    override fun onPause() {
        super.onPause()
        saveFile()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("currentFileName", currentFileName)
    }
}