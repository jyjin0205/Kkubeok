package com.example.kkubeok.screen

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.content.Context.VIBRATOR_SERVICE
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
    var isRunning by remember{mutableStateOf(true)}
    val context=LocalContext.current
    val vibrator=remember{
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    var isCompleted by remember {mutableStateOf(false)}
    var blinkRed by remember {mutableStateOf(false)}

    LaunchedEffect(isRunning){
        while(totalSeconds>0&&isRunning){
            delay(1000L)
            totalSeconds--
        }

        if(totalSeconds==0 && !isCompleted){
            isCompleted=true
            // Vibrating
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
                val pattern=longArrayOf(0,500,500) // start, 500ms vibrate, 500ms rest iteratively
                val effect=VibrationEffect.createWaveform(pattern, 0) // 0 == iterative
                vibrator.vibrate(effect)
            }else{
                vibrator.vibrate(longArrayOf(0,500,500), 0)
            }
        }

        if(isCompleted){
            while(true){
                blinkRed=true
                delay(300L)
                blinkRed=false
                delay(300L)
            }
        }
    }

    val displayHour=totalSeconds/3600
    val displayMinute=(totalSeconds%3600)/60
    val displaySecond=totalSeconds %60

    Scaffold(
        containerColor=if(blinkRed) Color.Red.copy(alpha=0.15f) else Color.White,
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

            if(!isCompleted){
                // Pause and Resume
                Button(
                    onClick = {
                        isRunning=!isRunning
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text(
                        if(isRunning) "Pause" else "Resume",
                        color=Color.White
                    )
                }

                Spacer(modifier=Modifier.height(8.dp))
                // Delete Button
                Button(
                    onClick = {
                        navController?.navigate("Alarm"){
                            popUpTo("Alarm"){inclusive=true}
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text("Delete", color = Color.White)
                }
            } else {
                Button(
                    onClick={
                        vibrator.cancel()
                        navController?.navigate("alarm"){
                            popUpTo("alarm"){inclusive=true}
                        }
                    },
                    modifier=Modifier.fillMaxWidth(),
                    colors=ButtonDefaults.buttonColors(containerColor=Color.Black)
                ){
                    Text("Confirm", color=Color.White)
                }
            }
        }
    }
}
