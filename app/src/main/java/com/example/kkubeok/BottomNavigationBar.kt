package com.example.kkubeok

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

@Composable
fun BottomNavigationBar(navController: NavHostController){
    NavigationBar{
        NavigationBarItem(
            icon={ Icon(Icons.Default.Home, contentDescription="Home") },
            selected=false,
            onClick={navController.navigate("Home")}
        )
        NavigationBarItem(
            icon={ Icon(Icons.Default.Timeline, contentDescription="Timeline") },
            selected=false,
            onClick={navController.navigate("Timeline")}
        )
        NavigationBarItem(
            icon={ Icon(Icons.Default.Add, contentDescription="Detecting") },
            selected=false,
            onClick={navController.navigate("Detecting")}
        )
        NavigationBarItem(
            icon={ Icon(Icons.Default.BarChart, contentDescription="Analysis") },
            selected=false,
            onClick={/* TODO: Go to Analysis*/}
        )
        NavigationBarItem(
            icon={ Icon(Icons.Default.Alarm, contentDescription="Alarm") },
            selected=false,
            onClick={
                navController.navigate("Alarm")
            }
        )
    }
}