package com.example.aaosnavapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcel
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment

class MainActivity : AppCompatActivity() {

    private val PREFS_NAME = "NavStatePrefs"
    private val STATE_KEY = "NavControllerState"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val isLauncherLaunch = intent?.action == Intent.ACTION_MAIN &&
                intent.hasCategory(Intent.CATEGORY_LAUNCHER)
        val isFromHistory = ((intent?.flags ?: 0) and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        if (isLauncherLaunch && !isFromHistory) {
            // Fresh launch from Launcher. Clear any saved state so it defaults to RootFragment.
            prefs.edit().clear().apply()
        } else {
            // Launched from Recents or another app (like Car Settings). Restore the backstack state.
            val savedStateString = prefs.getString(STATE_KEY, null)
            if (savedStateString != null) {
                try {
                    val bytes = Base64.decode(savedStateString, 0)
                    val parcel = Parcel.obtain()
                    parcel.unmarshall(bytes, 0, bytes.size)
                    parcel.setDataPosition(0)
                    val bundle = Bundle.CREATOR.createFromParcel(parcel)
                    parcel.recycle()
                    navController.restoreState(bundle)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Save the exact navigation backstack state before the app is paused.
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val bundle = navHostFragment.navController.saveState()
        
        if (bundle != null) {
            val parcel = Parcel.obtain()
            bundle.writeToParcel(parcel, 0)
            val bytes = parcel.marshall()
            parcel.recycle()
            
            val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(STATE_KEY, Base64.encodeToString(bytes, 0)).apply()
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // When the user presses the Home button, we instantly kill the Activity.
        // This physically destroys the UI from the OS's memory, ensuring the OS CANNOT
        // hold onto a visual snapshot of SecondFragment. The next launch will be a clean
        // Cold Start without any historical flicker.
        finish()
    }
}
