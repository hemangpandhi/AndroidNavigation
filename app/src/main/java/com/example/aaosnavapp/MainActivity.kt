package com.example.aaosnavapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val isMainAction = intent?.action == Intent.ACTION_MAIN
        val isFromHistory = ((intent?.flags ?: 0) and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0

        val stateToRestore = if (isMainAction && !isFromHistory) null else savedInstanceState

        super.onCreate(stateToRestore)
        setContentView(R.layout.activity_main)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        
        val isMainAction = intent?.action == Intent.ACTION_MAIN
        val isFromHistory = ((intent?.flags ?: 0) and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0

        if (isMainAction && !isFromHistory) {
            // Synchronously reset the navigation graph
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as androidx.navigation.fragment.NavHostFragment
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
