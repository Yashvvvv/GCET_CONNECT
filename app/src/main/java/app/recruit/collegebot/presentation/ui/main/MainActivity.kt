package app.recruit.collegebot.presentation.ui.main

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.getValue
import app.recruit.collegebot.presentation.ui.chat.ChatPage
import app.recruit.collegebot.presentation.ui.splash.SplashScreen
import app.recruit.collegebot.presentation.ui.study.StudyScreen
import com.example.collegebot.ui.theme.CollegeBotTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CollegeBotTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                NavHost(
                    navController    = navController,
                    startDestination = "splash"
                ) {
                    composable("splash") {
                        SplashScreen(navController)
                    }

                    composable("chat") {
                        BackHandler(enabled = currentRoute == "chat") { showExitDialog() }
                        ChatPage(
                            modifier     = Modifier.fillMaxSize(),
                            onStudyClick = { navController.navigate("study") }
                        )
                    }

                    composable("study") {
                        StudyScreen(
                            modifier      = Modifier.fillMaxSize(),
                            navController = navController
                        )
                    }
                }
            }
        }
    }

    private fun showExitDialog() {
        AlertDialog.Builder(this)
            .setTitle("Exit")
            .setMessage("Exit GCET Connect?")
            .setPositiveButton("Exit") { _, _ -> finish() }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
