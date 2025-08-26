package com.example.kotlintexteditor

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.kotlintexteditor.databinding.ActivityHomeBinding
import java.io.File

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var adapter: ArrayAdapter<String>
    private var allFiles: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        setupUI()
        loadFiles()
    }

    private fun setupUI() {


        // New File button
        binding.btnNewFile.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        // Setup file search
        binding.searchBox.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().lowercase()
                val filtered = allFiles.filter { it.lowercase().contains(query) }
                updateList(filtered)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // File click listener
        binding.listViewFiles.setOnItemClickListener { _, _, position, _ ->
            val fileName = adapter.getItem(position)
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("OPEN_FILE_NAME", fileName)
            startActivity(intent)
        }
    }

    private fun loadFiles() {
        allFiles = filesDir.listFiles()?.map { it.name }?.sortedByDescending {
            File(filesDir, it).lastModified()
        } ?: emptyList()
        updateList(allFiles)
    }

    private fun updateList(fileList: List<String>) {
        adapter = object : ArrayAdapter<String>(this, R.layout.list_item_file, R.id.fileName, fileList) {}
        binding.listViewFiles.adapter = adapter
    }
}
