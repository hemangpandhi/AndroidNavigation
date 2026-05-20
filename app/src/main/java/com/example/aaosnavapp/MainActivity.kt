package com.example.aaosnavapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            setRecentsScreenshotEnabled(false)
        }
        
        val isLauncherLaunch = intent?.action == Intent.ACTION_MAIN &&
                intent.hasCategory(Intent.CATEGORY_LAUNCHER)
        val isFromHistory = ((intent?.flags ?: 0) and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0

        val stateToRestore = if (isLauncherLaunch && !isFromHistory) null else savedInstanceState

        super.onCreate(stateToRestore)
        setContentView(R.layout.activity_main)
    }

    override fun onPause() {
        super.onPause()
        // Hide the entire UI before the OS takes a snapshot.
        // This guarantees the OS snapshot will be completely blank, 
        // preventing any chance of an OS-level flicker of the old fragment.
        findViewById<View>(R.id.nav_host_fragment).visibility = View.INVISIBLE
    }

    override fun onResume() {
        super.onResume()
        // Restore visibility on the next frame, giving onNewIntent 
        // and Jetpack Navigation time to completely swap the fragments.
        val navHost = findViewById<View>(R.id.nav_host_fragment)
        navHost.post {
            navHost.visibility = View.VISIBLE
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        
        val isLauncherLaunch = intent?.action == Intent.ACTION_MAIN &&
                intent.hasCategory(Intent.CATEGORY_LAUNCHER)
        val isFromHistory = ((intent?.flags ?: 0) and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0

        if (isLauncherLaunch && !isFromHistory) {
            val navHostContainer = findViewById<ViewGroup>(R.id.nav_host_fragment)
            
            // Brutal force clear: remove all old fragment views immediately 
            // so the app process physically cannot draw them.
            navHostContainer.removeAllViews()
            
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController
            
            val startDestinationId = navController.graph.startDestinationId
            val options = androidx.navigation.NavOptions.Builder()
                .setPopUpTo(startDestinationId, true)
                .setEnterAnim(0)
                .setExitAnim(0)
                .setPopEnterAnim(0)
                .setPopExitAnim(0)
                .build()
                
            navController.navigate(startDestinationId, null, options)
            supportFragmentManager.executePendingTransactions()
        }
    }
}
