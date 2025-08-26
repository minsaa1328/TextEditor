import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import java.util.Stack

class UndoHelper(private val editText: EditText) {

    private val undoStack = Stack<EditAction>()
    private val redoStack = Stack<EditAction>()
    private var isEditing = false
    var isProgrammaticChange = false

    data class EditAction(val text: CharSequence, val selectionStart: Int, val selectionEnd: Int)

    private val watcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (!isEditing && !isProgrammaticChange && s != null) {
                // Store plain text without spans for undo/redo
                val plainText = s.toString()
                undoStack.push(EditAction(plainText, editText.selectionStart, editText.selectionEnd))
                redoStack.clear()
            }
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    fun start() {
        undoStack.push(EditAction(editText.text.toString(), editText.selectionStart, editText.selectionEnd))
        editText.addTextChangedListener(watcher)
    }

    fun stop() {
        editText.removeTextChangedListener(watcher)
    }

    fun undo() {
        if (undoStack.size > 1) {
            isEditing = true
            val current = undoStack.pop()
            redoStack.push(EditAction(editText.text.toString(), editText.selectionStart, editText.selectionEnd))
            val previous = undoStack.peek()
            setTextProgrammatically(previous.text, previous.selectionStart, previous.selectionEnd)
            isEditing = false
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            isEditing = true
            val next = redoStack.pop()
            undoStack.push(EditAction(editText.text.toString(), editText.selectionStart, editText.selectionEnd))
            setTextProgrammatically(next.text, next.selectionStart, next.selectionEnd)
            isEditing = false
        }
    }

    fun setTextProgrammatically(text: CharSequence, selectionStart: Int, selectionEnd: Int) {
        isProgrammaticChange = true
        editText.setText(text)
        editText.setSelection(
            selectionStart.coerceIn(0, text.length),
            selectionEnd.coerceIn(0, text.length)
        )
        isProgrammaticChange = false
    }
}