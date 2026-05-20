package com.example.aaosnavapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private val REQUEST_CODE_MAIN = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startMainActivity()
    }

    override fun onRestart() {
        super.onRestart()
        // When clearTaskOnLaunch destroys MainActivity on a Launcher click, 
        // SplashActivity is restarted. We start MainActivity fresh.
        startMainActivity()
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_MAIN)
        // We use overridePendingTransition to prevent any visual delay/animation 
        // when transitioning from this transparent routing activity to MainActivity.
        overridePendingTransition(0, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_MAIN) {
            // MainActivity finished normally (e.g., user pressed the Back button).
            // We must finish the SplashActivity too so the app actually closes.
            finish()
        }
    }
}
