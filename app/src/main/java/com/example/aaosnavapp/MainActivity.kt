package com.example.aaosnavapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val isLauncherLaunch = intent?.action == Intent.ACTION_MAIN &&
                intent.hasCategory(Intent.CATEGORY_LAUNCHER)
        val isFromHistory = ((intent?.flags ?: 0) and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0

        val stateToRestore = if (isLauncherLaunch && !isFromHistory) null else savedInstanceState

        super.onCreate(stateToRestore)
        setContentView(R.layout.activity_main)
    }

    override fun onPause() {
        super.onPause()
        // SNAPSHOT FORGERY: Before the OS takes a snapshot of the app going to the background,
        // we forcefully display a Cover View that looks exactly like RootFragment.
        // This guarantees that the OS snapshot slot contains a picture of RootFragment,
        // so when the Car Launcher animates the app to the front, it is flawless.
        findViewById<View>(R.id.snapshot_cover).visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        // Once the OS transition animation completes and the app is fully resumed,
        // we instantly hide the forged RootFragment cover.
        // If launched from Recents, the real SecondFragment underneath is revealed.
        // If launched from Launcher, onNewIntent has already swapped the fragment underneath to RootFragment.
        val cover = findViewById<View>(R.id.snapshot_cover)
        cover.post {
            cover.visibility = View.GONE
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        
        val isLauncherLaunch = intent?.action == Intent.ACTION_MAIN &&
                intent.hasCategory(Intent.CATEGORY_LAUNCHER)
        val isFromHistory = ((intent?.flags ?: 0) and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0

        if (isLauncherLaunch && !isFromHistory) {
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
