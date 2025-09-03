package app.recruit.collegebot.utils

import android.content.Context

// This approach keeps your API key secure and not exposed in Git repository
object Constants {
    // Reads com.example.collegebot.BuildConfig.GEMINI_API_KEY via reflection at runtime
    val API_KEY: String by lazy {
        try {
            val clazz = Class.forName("com.example.collegebot.BuildConfig")
            val field = clazz.getField("GEMINI_API_KEY")
            (field.get(null) as? String).orEmpty()
        } catch (_: Throwable) {
            "" // Ensure GEMINI_API_KEY is set in gradle.properties and wired via build.gradle.kts
        }
    }

    // For call site compatibility (some pass Application/Context)
    fun getApiKey(@Suppress("UNUSED_PARAMETER") context: Context? = null): String = API_KEY
}
