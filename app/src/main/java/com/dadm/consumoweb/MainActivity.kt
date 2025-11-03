package com.dadm.consumoweb

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.dadm.consumoweb.vistas.EntidadesApp
import com.dadm.consumoweb.vistas.EntidadesViewModel
import com.dadm.consumoweb.ui.theme.ConsumoWebTheme

class MainActivity : ComponentActivity() {

    private val viewModel: EntidadesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ConsumoWebTheme {
                EntidadesApp(viewModel = viewModel)
            }
        }
    }
}