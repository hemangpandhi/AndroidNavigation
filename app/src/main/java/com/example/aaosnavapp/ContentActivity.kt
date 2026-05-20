package com.example.aaosnavapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class ContentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This activity actually hosts the Jetpack Navigation fragment.
        setContentView(R.layout.activity_main)
    }
}
