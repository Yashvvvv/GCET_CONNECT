package app.recruit.collegebot.utils

import android.content.Context
import com.example.collegebot.BuildConfig

// Keep API key in gradle.properties; read via BuildConfig so it's not exposed in the repo
object Constants {
    // Value comes from: app/build.gradle.kts -> buildConfigField("String", "GEMINI_API_KEY", ...)
    // and gradle.properties -> GEMINI_API_KEY=...
    const val API_KEY: String = BuildConfig.GEMINI_API_KEY

    // Kept for call-site compatibility (some call with Application/Context)
    fun getApiKey(@Suppress("UNUSED_PARAMETER") context: Context? = null): String = API_KEY
}