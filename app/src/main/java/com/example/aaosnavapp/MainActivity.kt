package com.example.aaosnavapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Handle intent if launched directly
        handleNavigationReset(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent) // Ensure we have the latest intent saved
        
        handleNavigationReset(intent)
    }

    private fun handleNavigationReset(intent: Intent?) {
        intent ?: return

        // Check if launched from Launcher
        val isLauncherLaunch = intent.action == Intent.ACTION_MAIN &&
                intent.hasCategory(Intent.CATEGORY_LAUNCHER)

        // Check if launched from history (Recents)
        val isFromHistory = (intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0

        if (isLauncherLaunch && !isFromHistory) {
            // Find NavController
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController
            
            // Pop to the start destination, effectively resetting the back stack
            val startDestinationId = navController.graph.startDestinationId
            navController.popBackStack(startDestinationId, inclusive = false)
        }
    }
}
