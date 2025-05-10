package com.example.kkubeok.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.example.kkubeok.BottomNavigationBar
import kotlinx.coroutines.delay

@Composable
fun CountdownScreen(navController: NavHostController? = null) {
    Scaffold(
        bottomBar = {
            navController?.let { BottomNavigationBar(navController = it) }
        }
    ){paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Countdown", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)}
    }
}
