// StartActivity.kt
package com.example.pacecolor

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val runNameInput: EditText = findViewById(R.id.runNameInput)
        val nextButton: Button = findViewById(R.id.nextButton)

        nextButton.setOnClickListener {
            val runName = runNameInput.text.toString().trim()
            if (runName.isNotEmpty()) {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("runName", runName)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Bitte gib einen Namen für den Lauf ein", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
