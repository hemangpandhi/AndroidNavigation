package com.example.aaosnavapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val REQUEST_CODE_CONTENT = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startContentActivity()
    }

    override fun onRestart() {
        super.onRestart()
        // clearTaskOnLaunch will have destroyed ContentActivity.
        // We restart it fresh here.
        startContentActivity()
    }

    private fun startContentActivity() {
        val intent = android.content.Intent(this, ContentActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_CONTENT)
        overridePendingTransition(0, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CONTENT) {
            finish()
        }
    }
}
