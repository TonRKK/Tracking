package com.chornerman.whereismystuff.screens

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.chornerman.whereismystuff.R

class menu : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        val button = findViewById<Button>(R.id.btPacking)
        button.setOnClickListener {
            val intent = Intent(this@menu, MainActivity::class.java)
            startActivity(intent)
        }
    }
}