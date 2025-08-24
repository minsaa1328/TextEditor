package com.example.kotlintexteditor

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class OnboardingActivity2 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding2)

        findViewById<Button>(R.id.nextButton).setOnClickListener {
            startActivity(Intent(this, OnboardingActivity3::class.java))
            finish()
        }

        findViewById<TextView>(R.id.skipText).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }
}
