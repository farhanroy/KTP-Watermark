package com.example.ktpwatermark

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.graphics.ExperimentalGraphicsApi
import com.example.ktpwatermark.ui.screens.HomeScreen
import com.example.ktpwatermark.ui.theme.KTPWatermarkTheme

class MainActivity : ComponentActivity() {
    @ExperimentalMaterialApi
    @ExperimentalGraphicsApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KTPWatermarkTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    HomeScreen()
                }
            }
        }
    }

}



