package com.ibader.citoyenpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ibader.citoyenpro.data.local.database.AppDatabase
import com.ibader.citoyenpro.data.repository.UserRepository
import com.ibader.citoyenpro.ui.navigation.AppNavHost
import com.ibader.citoyenpro.ui.theme.CitoyenProTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(applicationContext)
        val userRepository = UserRepository(database.userDao())

        setContent {
            CitoyenProTheme {
                AppNavHost(userRepository = userRepository)
            }
        }
    }
}
