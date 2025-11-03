package com.dadm.consumoweb

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge // 🛠️ Importante para el margen superior
import com.dadm.consumoweb.vistas.EntidadesApp
import com.dadm.consumoweb.ui.theme.ConsumoWebTheme


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            ConsumoWebTheme {
                EntidadesApp()
            }
        }
    }
}