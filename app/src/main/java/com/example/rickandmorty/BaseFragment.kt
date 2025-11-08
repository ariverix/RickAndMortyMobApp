package com.example.rickandmorty

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment() {

    protected val logTag: String
        get() = this::class.java.simpleName

    protected fun logEvent(message: String) {
        Log.d(logTag, message)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logEvent("onCreate() вызван")
    }

    override fun onStart() {
        super.onStart()
        logEvent("onStart() вызван")
    }

    override fun onResume() {
        super.onResume()
        logEvent("onResume() вызван")
    }

    override fun onPause() {
        super.onPause()
        logEvent("onPause() вызван")
    }

    override fun onStop() {
        super.onStop()
        logEvent("onStop() вызван")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        logEvent("onDestroyView() вызван")
    }

    override fun onDestroy() {
        super.onDestroy()
        logEvent("onDestroy() вызван")
    }
}
