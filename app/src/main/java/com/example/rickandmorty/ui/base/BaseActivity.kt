package com.example.rickandmorty.ui.base

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {

    private val TAG = this::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate() вызван")
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart() вызван")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d(TAG, "onRestart() вызван")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume() вызван")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause() вызван")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop() вызван")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy() вызван")
    }
}