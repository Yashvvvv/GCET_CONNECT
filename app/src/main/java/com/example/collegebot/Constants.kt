package com.example.collegebot

import android.content.Context
import com.example.collegebot.BuildConfig
import com.example.collegebot.R

// Handle the API key directly from gradle.properties
object Constants {
    // API key from BuildConfig
    const val API_KEY = BuildConfig.GEMINI_API_KEY
    
    // Get the API key with a fallback to the string resource
    fun getApiKey(context: Context? = null): String {
        // Try BuildConfig first
        if (API_KEY.isNotEmpty()) {
            return API_KEY
        }
        
        // Fall back to string resource if context is available
        return context?.getString(R.string.gemini_api_key) ?: "AIzaSyDE9UlxG64A8CRXQCSuAw-LJwM0rgnl460"
    }
} 