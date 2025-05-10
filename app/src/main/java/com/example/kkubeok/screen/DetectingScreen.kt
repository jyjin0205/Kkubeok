package com.example.kkubeok.screen

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.example.kkubeok.ui.theme.KkubeokTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

import com.example.kkubeok.BottomNavigationBar

@Composable
fun DetectingScreen(navController: NavHostController?=null){
    val context=LocalContext.current
    val sensorManager=remember{context.getSystemService(Context.SENSOR_SERVICE)as SensorManager }

    val accelerometer =remember{sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)}
    val gravity=remember{sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)}
    val gyroscope=remember{sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)}

    var accel by remember { mutableStateOf(Triple(0f, 0f, 0f)) }
    var gravityVal by remember { mutableStateOf(Triple(0f, 0f, 0f)) }
    var gyro by remember { mutableStateOf(Triple(0f, 0f, 0f)) }

    var totalTime by remember { mutableStateOf(0L) }
    var currentTime by remember { mutableStateOf(0L) }
    var startTimestamp by remember { mutableStateOf<Long?>(null) }

    val coroutineScope = rememberCoroutineScope()
    var timerJob by remember { mutableStateOf<Job?>(null) }
    var isListening by remember { mutableStateOf(false) }

    val listener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    val (x, y, z) = it.values
                    when (it.sensor.type) {
                        Sensor.TYPE_ACCELEROMETER -> accel = Triple(x, y, z)
                        Sensor.TYPE_GRAVITY -> gravityVal = Triple(x, y, z)
                        Sensor.TYPE_GYROSCOPE -> gyro = Triple(x, y, z)
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }

    Scaffold(
        bottomBar={
            navController?.let{
                BottomNavigationBar(navController=it)
            }
        }
    ){ paddingValues->
        Column(
            modifier=Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal=24.dp),
            horizontalAlignment=Alignment.CenterHorizontally,
            verticalArrangement=Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier=Modifier.height(16.dp))

            // Header
            Text(
                text="Detecting",
                style=MaterialTheme.typography.headlineSmall,
                fontWeight= FontWeight.Bold,
                modifier=Modifier.align(Alignment.Start)
            )

            // Day Block
            val todayDate by produceState(initialValue = getFormattedToday()) {
                while (true) {
                    value = getFormattedToday()
                    delay(60 * 1000L)
                }
            }
            Text(
                text = todayDate,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                fontSize=18.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            // Time Block
            Row(
                modifier=Modifier.fillMaxWidth(),
                horizontalArrangement=Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
            ){
                // Real World Current Time
                Card(
                    modifier=Modifier.weight(1f)
                        .padding(4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Current Time", fontWeight = FontWeight.Medium, fontSize=15.sp, maxLines=1)
                        Spacer(modifier = Modifier.height(8.dp))
                        val currentClock by produceState(initialValue=getCurrentHourMinute()){
                            while(true){
                                value=getCurrentHourMinute()
                                delay(60*1000L)
                            }
                        }
                        Text(currentClock, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Detecting Time Block
                Card(
                    modifier=Modifier.weight(1f)
                        .padding(4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Detecting Time", fontWeight = FontWeight.Medium, fontSize=15.sp, maxLines=1)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(formatSeconds(totalTime+currentTime), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier=Modifier.height(8.dp))

            // Sensor Values
            SensorTable(accel, gravityVal, gyro)

            Spacer(modifier=Modifier.height(8.dp))

            // Start/Stop Button
            var isSensing by remember{mutableStateOf(false)}

            Button(
                onClick={
                    if(!isSensing){
                        // Start
                        sensorManager.registerListener(listener, accelerometer, 10000)
                        sensorManager.registerListener(listener, gravity, 10000)
                        sensorManager.registerListener(listener, gyroscope, 10000)
                        startTimestamp=System.currentTimeMillis()
                        timerJob=coroutineScope.launch{
                            while(true){
                                delay(1000L)
                                currentTime = (System.currentTimeMillis() - (startTimestamp ?: 0L)) / 1000
                            }
                        }
                    } else {
                        // Stop
                        sensorManager.unregisterListener(listener)
                        timerJob?.cancel()
                        totalTime+=currentTime
                        currentTime=0L
                    }
                    isSensing=!isSensing
                },
                modifier=Modifier
                    .fillMaxWidth()
                    .padding(vertical=4.dp),
                colors=ButtonDefaults.buttonColors(containerColor= Color.Black)
            )
            {
                Text(
                    if(isSensing) "Stop" else "Start",
                    color=Color.White)
            }
        }
    }
}
fun getFormattedToday(): String{
    val calendar=Calendar.getInstance()
    val day=calendar.get(Calendar.DAY_OF_MONTH)
    val month = SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.time)
    val weekday = SimpleDateFormat("EEE", Locale.getDefault()).format(calendar.time)
    return "$day $month ($weekday)"
}
fun getCurrentHourMinute(): String {
    val calendar = Calendar.getInstance()
    val hour=calendar.get(Calendar.HOUR_OF_DAY)
    val minute=calendar.get(Calendar.MINUTE)
    return "%02dh %02dm".format(hour,minute)
}

@Composable
fun SensorTable(accel:Triple<Float,Float,Float>,gravity:Triple<Float,Float,Float>, gyro:Triple<Float,Float,Float>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(12.dp)){
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){
                Text("Accel", fontWeight=FontWeight.Bold)
                Text("Gravity", fontWeight=FontWeight.Bold)
                Text("Gyro", fontWeight=FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("X %.2f".format(accel.first))
                Text("X %.2f".format(gravity.first))
                Text("X %.2f".format(gyro.first))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Y %.2f".format(accel.second))
                Text("Y %.2f".format(gravity.second))
                Text("Y %.2f".format(gyro.second))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Z %.2f".format(accel.third))
                Text("Z %.2f".format(gravity.third))
                Text("Z %.2f".format(gyro.third))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetectingScreenPreview() {
    val dummyNavController=rememberNavController()
    KkubeokTheme {
        DetectingScreen(navController=dummyNavController)
    }
}
