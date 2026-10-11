package ru.hdlabs.electrosmeta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ru.hdlabs.electrosmeta.navigation.AppNavHost
import ru.hdlabs.electrosmeta.ui.theme.HdLabsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HdLabsTheme {
                AppNavHost()
            }
        }
    }
}
