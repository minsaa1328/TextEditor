package com.example.kotlintexteditor.utils


import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import java.util.*

class UndoHelper(private val editText: EditText) {

    private val undoStack = Stack<String>()
    private val redoStack = Stack<String>()
    private var isEditing = false

    private val watcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (!isEditing) {
                undoStack.push(s.toString())
                redoStack.clear()
            }
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    fun start() {
        undoStack.push(editText.text.toString()) // initial state
        editText.addTextChangedListener(watcher)
    }

    fun stop() {
        editText.removeTextChangedListener(watcher)
    }

    fun undo() {
        if (undoStack.size > 1) {
            isEditing = true
            val current = undoStack.pop()
            redoStack.push(current)
            editText.setText(undoStack.peek())
            editText.setSelection(undoStack.peek().length)
            isEditing = false
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            isEditing = true
            val next = redoStack.pop()
            undoStack.push(next)
            editText.setText(next)
            editText.setSelection(next.length)
            isEditing = false
        }
    }
}
