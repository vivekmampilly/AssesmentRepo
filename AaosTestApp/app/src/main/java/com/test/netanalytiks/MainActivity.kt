package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.test.netanalytiks.CarPropertyService
import com.test.netanalytiks.viewmodel.CarDataViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        startService(Intent(this, CarPropertyService::class.java))
        setContent {
            MyApplicationTheme {
                SpeedWarningCompose(this)

            }
        }
    }
}

@Composable
fun SpeedWarningCompose(context: Context, modifier: Modifier = Modifier, carDataViewModel: CarDataViewModel = viewModel()) {
    carDataViewModel.connectToService(context)
    val carDataUiState = carDataViewModel.carDataState.collectAsState()
    Column (verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
        ) {
        /**
         * Shows max speed
         */
        Text(
            text = "Max Speed Limit:  ${carDataUiState.value.carMaxSpeed} KM/Hr",
            modifier = modifier
        )

        /**
         * Shows current speed and changes color to red when limit exceeds
         */
        Text(
            text = if (carDataUiState.value.carCurrentSpeed > carDataUiState.value.carMaxSpeed)
                "Max Speed Warning:  ${carDataUiState.value.carCurrentSpeed} KM/Hr !!!!!"
                else "Speed : ${carDataUiState.value.carCurrentSpeed}",
            color = if (carDataUiState.value.carCurrentSpeed > carDataUiState.value.carMaxSpeed) Color.Red
                else Color.Blue,
            modifier = modifier
        )

        Text(
            text = if (carDataUiState.value.carCurrentSpeed > carDataUiState.value.carMaxSpeed)
                "Please reduce Speed !!!!" else "",
            color = Color.Red,
            modifier = modifier
        )

    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        //SpeedWarningCompose()
    }
}