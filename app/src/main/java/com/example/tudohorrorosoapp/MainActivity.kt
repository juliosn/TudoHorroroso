package com.example.tudohorrorosoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.example.tudohorrorosoapp.navigation.SetupNavigation
import com.example.tudohorrorosoapp.ui.theme.TudoHorrorosoAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TudoHorrorosoAppTheme {
                val navController = rememberNavController()

                Surface(color = MaterialTheme.colorScheme.background) {
                    SetupNavigation(navController)
                }
            }
        }
    }
}
