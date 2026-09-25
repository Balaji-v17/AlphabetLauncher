package com.balaji.v.alphabetlauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.balaji.v.alphabetlauncher.ui.HomeScreen
import com.balaji.v.alphabetlauncher.ui.theme.AlphabetLauncherTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AlphabetLauncherTheme {
                HomeScreen()
            }
        }
    }
}