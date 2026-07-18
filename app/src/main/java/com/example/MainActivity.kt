package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.MainViewModel
import com.example.ui.ThemeMode
import com.example.ui.AppContent
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request System Notification permission on launch for Android 13+
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission("android.permission.POST_NOTIFICATIONS") != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf("android.permission.POST_NOTIFICATIONS"), 101)
            }
        }

        // Edge-To-Edge drawing safe boundaries.
        enableEdgeToEdge()

        // Disable screenshot capability to protect materials by default
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val useDarkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            // Dynamically allow screenshots only for admins or user 'saqi'
            val loggedInUser by viewModel.loggedInUser.collectAsState()
            androidx.compose.runtime.LaunchedEffect(loggedInUser) {
                val user = loggedInUser
                val isScreenshotAllowed = user?.role == "ADMIN" ||
                        user?.username?.equals("saqi", ignoreCase = true) == true ||
                        user?.fullName?.equals("saqi", ignoreCase = true) == true

                if (isScreenshotAllowed) {
                    window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
                } else {
                    window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
                }
            }

            val appSettings by viewModel.appSettings.collectAsState()
            val primaryHex = appSettings["theme_primary_color"]
            val secondaryHex = appSettings["theme_secondary_color"]

            val primaryColor = if (!primaryHex.isNullOrBlank()) {
                try {
                    androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(primaryHex))
                } catch (e: Exception) {
                    null
                }
            } else null

            val secondaryColor = if (!secondaryHex.isNullOrBlank()) {
                try {
                    androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(secondaryHex))
                } catch (e: Exception) {
                    null
                }
            } else null

            MyApplicationTheme(
                primaryColor = primaryColor,
                secondaryColor = secondaryColor
            ) {
                AppContent(viewModel = viewModel)
            }
        }
    }
}
