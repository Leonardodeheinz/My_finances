package com.example.my_finances

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.my_finances.ui.navigation.NavGraph
import com.example.my_finances.ui.navigation.Screen
import com.example.my_finances.ui.theme.My_financesTheme
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val startDestination = if (FirebaseAuth.getInstance().currentUser != null) {
            Screen.Home.route
        } else {
            Screen.Login.route
        }

        setContent {
            My_financesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph(startDestination = startDestination)
                }
            }
        }
    }
}


