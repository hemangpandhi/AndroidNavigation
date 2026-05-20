package com.example.aaosnavapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val isLauncherLaunch = intent?.action == Intent.ACTION_MAIN &&
                intent?.hasCategory(Intent.CATEGORY_LAUNCHER) == true
        val isFromHistory = ((intent?.flags ?: 0) and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0

        val stateToRestore = if (isLauncherLaunch && !isFromHistory) null else savedInstanceState

        super.onCreate(stateToRestore)
        setContentView(R.layout.activity_main)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        
        val isLauncherLaunch = intent?.action == Intent.ACTION_MAIN &&
                intent.hasCategory(Intent.CATEGORY_LAUNCHER)
        val isFromHistory = ((intent?.flags ?: 0) and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0

        if (isLauncherLaunch && !isFromHistory) {
            // Restart the activity completely to clear the back stack without a flicker
            val restartIntent = Intent(this, MainActivity::class.java).apply {
                action = Intent.ACTION_MAIN
                addCategory(Intent.CATEGORY_LAUNCHER)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(restartIntent)
            overridePendingTransition(0, 0)
            finish()
        }
    }
}
