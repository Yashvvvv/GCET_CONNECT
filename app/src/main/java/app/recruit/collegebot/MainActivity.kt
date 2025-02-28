package com.example.collegebot

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import android.window.SplashScreen
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.collegebot.ui.theme.CollegeBotTheme
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.activity.compose.BackHandler


class MainActivity : ComponentActivity() {
    private val chatViewModel: ChatViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ChatViewModel(application) as T
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CollegeBotTheme {
                val navController = rememberNavController()

                // Initialize hasSeenSplash as false to show splash screen first
                var hasSeenSplash by remember { mutableStateOf(false) }

                NavHost(
                    navController = navController,
                    startDestination = "splash"  // Always start with splash
                ) {
                    composable("splash") {
                        SplashScreen(navController)
                        hasSeenSplash = true
                    }

                    composable("chat") {
                        BackHandler {
                            // Show confirmation dialog when back is pressed
                            showExitDialog()
                        }
                        
                        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                            ChatPage(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding),
                                viewModel = chatViewModel
                            )
                        }
                    }
                }
            }
        }
    }

    private fun showExitDialog() {
        AlertDialog.Builder(this)
            .setTitle("Exit App")
            .setMessage("Do you want to exit the app?")
            .setPositiveButton("Yes") { _, _ ->
                finish() // Close the app
            }
            .setNegativeButton("No", null)
            .show()
    }
}
