package com.example.kotlintexteditor

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LaunchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.launch_screen)  // Make sure the XML layout file is named correctly.

        // Initialize the views
        val getStartedButton: Button = findViewById(R.id.getStartedButton)


        // Set up the "Get Started" button click listener
        getStartedButton.setOnClickListener {
            // Navigate to the next activity when the button is clicked
            val intent = Intent(this, OnboardingActivity1::class.java)  // Replace MainActivity with the actual target activity
            startActivity(intent)
            finish()  // Finish the current activity so it won't show up in the back stack
        }


    }
}
