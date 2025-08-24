package com.example.kotlintexteditor

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import com.example.kotlintexteditor.model.SyntaxRule
import com.example.kotlintexteditor.utils.*
import java.io.*

class MainActivity : AppCompatActivity() {

    private var isHighlighting = false

    private lateinit var editor: EditText
    private lateinit var lineNumbers: TextView
    private lateinit var statusBar: TextView
    private lateinit var languageSpinner: Spinner
    private lateinit var undoHelper: UndoHelper

    private var currentFileName = "untitled.txt"
    private val OPEN_FILE_REQUEST_CODE = 100
    private lateinit var fileListAdapter: ArrayAdapter<String>
    private var currentSyntaxRule: SyntaxRule? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editor = findViewById(R.id.editor)
        lineNumbers = findViewById(R.id.lineNumbers)
        statusBar = findViewById(R.id.statusBar)
        languageSpinner = findViewById(R.id.languageSpinner)

        undoHelper = UndoHelper(editor)
        undoHelper.start()

        findViewById<ImageButton>(R.id.btnNew).setOnClickListener { newFile() }
        findViewById<ImageButton>(R.id.btnOpen).setOnClickListener { showFilePickerDialog() }
        findViewById<ImageButton>(R.id.btnSave).setOnClickListener { saveFile() }
        findViewById<ImageButton>(R.id.btnCompile).setOnClickListener { compileCode() }

        val btnMenu = findViewById<ImageButton>(R.id.btnMenu)
        btnMenu.setOnClickListener { showPopupMenu(btnMenu) }

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
            override fun afterTextChanged(s: Editable?) {
                highlightEditor()
                updateStatus()
                updateLineNumbers()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun compileCode() {
        val cleanCode = editor.text
            .toString()
            .replace(Regex("\n+--- Compiling ---.*", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("\n+--- Compiler Output ---.*", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("\n+--- Runtime Output ---.*", RegexOption.DOT_MATCHES_ALL), "")
            .trim()

        editor.setText(cleanCode)
        editor.append("\n\n--- Compiling ---")

        ADBCompiler.compile(this, cleanCode) { compilerOutput, runtimeOutput ->
            runOnUiThread {
                editor.append("\n\n--- Compiler Output ---\n$compilerOutput")
                editor.append("\n\n--- Runtime Output ---\n$runtimeOutput")
                Toast.makeText(this, "Compilation and execution complete", Toast.LENGTH_SHORT).show()
            }
        }
    }





    private fun highlightEditor() {
        if (isHighlighting) return

        isHighlighting = true

        val code = editor.text.toString()
        val highlighted = if (currentSyntaxRule == null) {
            SyntaxHighlighter.highlightKotlinCode(code)
        } else {
            GenericSyntaxHighlighter.highlight(code, currentSyntaxRule!!)
        }

        val cursorPos = editor.selectionStart
        editor.setText(highlighted)
        editor.setSelection(minOf(cursorPos, highlighted.length))

        isHighlighting = false
    }

    private fun updateLineNumbers() {
        val lines = editor.lineCount
        val sb = StringBuilder()
        for (i in 1..lines) sb.append("$i\n")
        lineNumbers.text = sb.toString()
    }

    private fun updateStatus() {
        val wordCount = editor.text.split("\\s+".toRegex()).filter { it.isNotEmpty() }.size
        val charCount = editor.text.length
        statusBar.text = "$currentFileName | Words: $wordCount | Chars: $charCount"
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
}
