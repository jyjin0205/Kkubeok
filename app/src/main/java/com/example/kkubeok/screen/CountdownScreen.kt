package com.example.kkubeok.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.example.kkubeok.BottomNavigationBar
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun CountdownScreen(
    hour:Int,
    minute:Int,
    second:Int,
    navController: NavHostController? = null) {

    var totalSeconds by remember{mutableIntStateOf(hour*3600+minute * 60 + second)}

    LaunchedEffect(Unit){
        while(totalSeconds>0){
            delay(1000L)
            totalSeconds--
        }
    }

    val displayHour=totalSeconds/3600
    val displayMinute=(totalSeconds%3600)/60
    val displaySecond=totalSeconds %60

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
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier=Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                colors=CardDefaults.cardColors(containerColor=Color(0xFFEAE6F2)),
                shape= RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = String.format(
                            Locale.US,
                            "%02d:%02d:%02d",
                            displayHour,
                            displayMinute,
                            displaySecond
                        ),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier=Modifier.height(24.dp))
            // Pause and Resume
            Button(
                onClick = {/* TODO: Implement Pause*/
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Pause", color = Color.White)
            }

            Spacer(modifier=Modifier.height(8.dp))
            // Delete Button
            Button(
                onClick = {/* TODO: Implement Delete*/
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Delete", color = Color.White)
            }
        }
    }
}
