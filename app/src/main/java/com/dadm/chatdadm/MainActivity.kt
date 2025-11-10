package com.dadm.chatdadm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat // ¡Importación necesaria!
import com.dadm.chatdadm.ui.theme.ChatDADMTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        setContent {
            ChatDADMTheme {
                ChatScreen()
            }
        }
    }
}