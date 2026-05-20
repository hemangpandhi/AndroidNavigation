package com.example.aaosnavapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle

class SplashActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // This invisible router activity instantly launches the main content activity.
        val intent = Intent(this, MainActivity::class.java)
        // Forcefully clear the separate App task and restart it fresh
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        
        // Disable transition animations for the router
        @Suppress("DEPRECATION")
        overridePendingTransition(0, 0)
        
        finish()
    }
}
