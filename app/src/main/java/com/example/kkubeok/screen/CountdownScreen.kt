package com.example.kkubeok.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import kotlinx.coroutines.delay

@Composable
fun CountdownScreen(navBackStackEntry: NavBackStackEntry, onComplete: () -> Unit = {}) {
    val hour = navBackStackEntry.arguments?.getString("hour")?.toIntOrNull() ?: 0
    val minute = navBackStackEntry.arguments?.getString("minute")?.toIntOrNull() ?: 0
    val second = navBackStackEntry.arguments?.getString("second")?.toIntOrNull() ?: 0

    val totalInitialSeconds = hour * 3600 + minute * 60 + second
    var remainingSeconds by remember { mutableIntStateOf(totalInitialSeconds) }

    LaunchedEffect(key1 = remainingSeconds) {
        if (remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds--
        } else {
            onComplete()
        }
    }

    val displayHour = remainingSeconds / 3600
    val displayMinute = (remainingSeconds % 3600) / 60
    val displaySecond = remainingSeconds % 60

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = String.format("%02d:%02d:%02d", displayHour, displayMinute, displaySecond),
            fontSize = 48.sp
        )
    }
}
